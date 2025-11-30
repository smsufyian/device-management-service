import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension

plugins {
    java
    jacoco
    checkstyle
    pmd
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.jib)
    //alias(libs.plugins.spotless)
    alias(libs.plugins.owaspDependencyCheck)
}

group = "com.devices"
version = "0.0.1-SNAPSHOT"
description = "devices-api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
    }
    withSourcesJar()
    withJavadocJar()
}

val javaVersion = JavaVersion.toVersion(libs.versions.java.get())
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = javaVersion.toString()
    targetCompatibility = javaVersion.toString()
}

repositories { mavenCentral() }

jacoco { toolVersion = libs.versions.jacoco.get() }

//configure<SpotlessExtension> {
//    java {
//        target("src/**/java/**/*.java")
//        palantirJavaFormat("2.38.0")
//        trimTrailingWhitespace()
//    }
//}
//
//tasks.check {
//    dependsOn(tasks.spotlessCheck)
//}

configure<CheckstyleExtension> {
    toolVersion = libs.versions.checkstyle.get()

    val cfg = file("config/checkstyle/checkstyle.xml")
    if (cfg.exists()) {
        configFile = cfg
    } else {
        logger.warn("[build.gradle.kts] Checkstyle config not found at ${cfg.path}. Skipping explicit configFile.")
    }
}

tasks.withType<Checkstyle>().configureEach {
    isShowViolations = true
    maxWarnings = 0
    exclude("**/generated/**", "**/build/**")
}

configure<PmdExtension> {
    toolVersion = libs.versions.pmd.get()
    isConsoleOutput = true
    isIgnoreFailures = false
    ruleSets = listOf(
        "category/java/bestpractices.xml",
        "category/java/errorprone.xml",
        "category/java/multithreading.xml",
    )
}

tasks.withType<Pmd>().configureEach {
    exclude("**/generated/**", "**/build/**")
}

tasks.named<Pmd>("pmdMain").configure {
    exclude("**/generated/**")
}

tasks.named<Pmd>("pmdTest").configure {
    enabled = false
}

configure<DependencyCheckExtension> {
    val suppression = file("config/dependency-check/suppressions.xml")
    if (suppression.exists()) {
        suppressionFile = suppression.path
    } else {
        logger.warn("[build.gradle.kts] Dependency-Check suppressions not found at ${suppression.path}. Continuing without suppressions.")
    }
    failBuildOnCVSS = 7.0F
}

jib {
    dockerClient {
        executable = findDocker().toString()
    }
}

fun findDocker(): File {
    val envPath = System.getenv("DOCKER_EXECUTABLE")?.takeIf { it.isNotBlank() }?.let { File(it) }
    val commonPaths = listOf(
        "/usr/local/bin/docker",                                           // Intel Mac
        "/opt/homebrew/bin/docker",                                        // Apple Silicon Mac
        "/usr/bin/docker",                                                 // Linux
        "C:\\Program Files\\Docker\\Docker\\resources\\bin\\docker.exe",  // Windows
        "C:\\Program Files\\Docker\\resources\\bin\\docker.exe"            // Windows Alt
    )

    val found = sequence {
        if (envPath != null) yield(envPath)
        yieldAll(commonPaths.map { File(it) })
    }.firstOrNull { it.exists() }

    return found ?: throw GradleException(
        "Docker executable not found. Checked DOCKER_EXECUTABLE='${envPath?.path ?: "<not set>"}' and common paths: " +
                commonPaths.joinToString() + ". Please install Docker, ensure it's on PATH, or set DOCKER_EXECUTABLE."
    )
}

dependencies {
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.flyway.database.postgresql)
    testImplementation(libs.spring.boot.starter.test)
    developmentOnly(libs.spring.boot.docker.compose)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.archunit)
    runtimeOnly(libs.postgresql)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
    val isCI = System.getenv("CI") == "true"
    onlyIf { isCI }
    if (isCI) {
        systemProperty("testcontainers.enabled", "true")
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    onlyIf { file("${layout.buildDirectory.get().asFile}/jacoco/test.exec").exists() }
    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
    }
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification").configure {
    dependsOn(tasks.test)
    onlyIf {
        val isCI = System.getenv("CI") == "true"
        val hasExec = file("${layout.buildDirectory.get().asFile}/jacoco/test.exec").exists()
        isCI && hasExec
    }
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(files("build/classes/java/main"))
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = BigDecimal("0.80")
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.checkstyleMain, tasks.checkstyleTest)
    dependsOn(tasks.pmdMain, tasks.pmdTest)
    //TODO: Fix spotless java21 incompatibility issue
    //dependsOn(tasks.spotlessCheck)
    dependsOn(tasks.jacocoTestReport)
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
