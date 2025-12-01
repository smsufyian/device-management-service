import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension

plugins {
    java
    jacoco
    checkstyle
    pmd
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.jib)
    //TODO: Fix the incompatibility issue with java21
    //alias(libs.plugins.spotless)
    alias(libs.plugins.owaspDependencyCheck)
    alias(libs.plugins.lombok)
}

group = "com.devices"
version = "0.0.1-SNAPSHOT"
description = "device-management-api"

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
    maxWarnings = Int.MAX_VALUE
    val cfg = file("config/checkstyle/checkstyle.xml")
    if (cfg.exists()) {
        configFile = cfg
    } else {
        logger.warn("[build.gradle.kts] Checkstyle config not found at ${cfg.path}. Skipping explicit configFile.")
    }
    tasks.named<Checkstyle>("checkstyleTest").configure {
        enabled = false
    }
}

tasks.withType<Checkstyle>().configureEach {
    isShowViolations = true
    maxWarnings = Int.MAX_VALUE
    exclude("**/generated/**", "**/build/**")
}

configure<PmdExtension> {
    toolVersion = libs.versions.pmd.get()
    isConsoleOutput = true
    isIgnoreFailures = true
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
        "/usr/local/bin/docker",
        "/opt/homebrew/bin/docker",
        "/usr/bin/docker"
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
    annotationProcessor(libs.mapstruct.processor)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.webmvc.openapiui)
    implementation(libs.mapstruct)
    implementation(libs.flyway.database.postgresql)
    developmentOnly(libs.spring.boot.docker.compose)
    developmentOnly(libs.spring.boot.dev.tools)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.restassured)
    testImplementation(libs.archunit)
    runtimeOnly(libs.postgresql)
    testRuntimeOnly(libs.junit.platform.launcher)
}


tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

springBoot {
    buildInfo()
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
                value = "COVERED RATIO"
                minimum = BigDecimal("0.80")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED"
    )
    finalizedBy(tasks.jacocoTestReport)
}

tasks.check {
    dependsOn(tasks.checkstyleMain, tasks.checkstyleTest)
    dependsOn(tasks.pmdMain, tasks.pmdTest)
    //TODO: Fix spotless java21 incompatibility issue
    //dependsOn(tasks.spotlessCheck)
    dependsOn(tasks.jacocoTestReport)
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}