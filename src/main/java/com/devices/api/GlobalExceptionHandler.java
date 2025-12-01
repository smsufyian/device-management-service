package com.devices.api;

import com.devices.domain.DeviceFieldLockedException;
import com.devices.domain.DeviceInUseException;
import com.devices.domain.DeviceNotFoundException;
import com.devices.domain.ImmutableFieldViolationException;
import com.devices.domain.InvalidDeviceStateException;
import com.devices.domain.VersionConflictException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final URI VALIDATION_ERROR_TYPE = URI.create("https://api.example.com/errors/validation-error");

    private static final URI MALFORMED_JSON_TYPE = URI.create("https://api.example.com/errors/malformed-json");

    private static final URI NOT_FOUND_TYPE = URI.create("https://api.example.com/errors/device-not-found");

    private static final URI INVALID_STATE_TYPE = URI.create("https://api.example.com/errors/invalid-device-state");

    private static final URI INVALID_PARAMETER_TYPE = URI.create("https://api.example.com/errors/invalid-parameter");

    private static final URI CONFLICT_TYPE = URI.create("https://api.example.com/errors/conflict");

    private static final URI UNPROCESSABLE_TYPE = URI.create("https://api.example.com/errors/unprocessable-entity");

    private static final URI INTERNAL_ERROR_TYPE = URI.create("https://api.example.com/errors/internal-server-error");

    private static final URI DEVICE_IN_USE_TYPE = URI.create("https://api.example.com/errors/device-in-use");

    private static final String PARAMETER_KEY = "parameter";

    private static final String INVALID_PARAMETER_TITLE = "Invalid Parameter";

    private static final String ERROR_CODE = "errorCode";

    @Override
    protected ResponseEntity<@NonNull Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                           @NonNull HttpHeaders headers,
                                                                           @NonNull HttpStatusCode status,
                                                                           @NonNull WebRequest request) {
        ProblemDetail problem = buildProblemDetail(status, "Request validation failed", "Validation Error", VALIDATION_ERROR_TYPE, request);

        var errors = ex.getBindingResult().getFieldErrors().stream().
                map(err -> Map.of(
                        "field", err.getField(),
                        "message", err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid value",
                        "rejectedValue", err.getRejectedValue()
                )).
                toList();

        problem.setProperty("errors", errors);
        return createResponseEntity(problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<@NonNull Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                           @NonNull HttpHeaders headers,
                                                                           @NonNull HttpStatusCode status,
                                                                           @NonNull WebRequest request) {
        ProblemDetail problem = buildProblemDetail(status, "Malformed JSON request", "Malformed Request", MALFORMED_JSON_TYPE, request);
        return createResponseEntity(problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<@NonNull Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex,
                                                                                   @NonNull HttpHeaders headers,
                                                                                   @NonNull HttpStatusCode status,@NonNull WebRequest request) {
        String detail = "Missing required parameter '%s'. Expected type: %s.".formatted(ex.getParameterName(), ex.getParameterType());
        ProblemDetail problem = buildProblemDetail(status, detail, INVALID_PARAMETER_TITLE, INVALID_PARAMETER_TYPE, request);
        problem.setProperty(PARAMETER_KEY, ex.getParameterName());
        return createResponseEntity(problem, headers, status, request);
    }


    @ExceptionHandler(DeviceNotFoundException.class)
    ProblemDetail handleDeviceNotFound(DeviceNotFoundException ex, WebRequest request) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), "Device Not Found", NOT_FOUND_TYPE, request);
    }

    @ExceptionHandler(InvalidDeviceStateException.class)
    ProblemDetail handleInvalidDeviceState(InvalidDeviceStateException ex, WebRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), "Invalid Device State", INVALID_STATE_TYPE, request);
    }

    @ExceptionHandler(DeviceInUseException.class)
    ProblemDetail handleDeviceInUse(DeviceInUseException ex, WebRequest request) {
        return buildProblemDetail(HttpStatus.CONFLICT, ex.getMessage(), "Device in use", DEVICE_IN_USE_TYPE, request);
    }

    @ExceptionHandler(ImmutableFieldViolationException.class)
    ProblemDetail handleImmutableField(ImmutableFieldViolationException ex, WebRequest request) {
        ProblemDetail problem = buildProblemDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), "Unprocessable Entity", UNPROCESSABLE_TYPE, request);
        problem.setProperty(ERROR_CODE, "IMMUTABLE_FIELD_VIOLATION");
        problem.setProperty("fieldName", ex.getFieldName());
        return problem;
    }

    @ExceptionHandler(DeviceFieldLockedException.class)
    ProblemDetail handleFieldLocked(DeviceFieldLockedException ex, WebRequest request) {
        ProblemDetail problem = buildProblemDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), "Unprocessable Entity", UNPROCESSABLE_TYPE, request);
        problem.setProperty("errorCode", "DEVICE_IN_USE_FIELD_LOCKED");
        problem.setProperty("fieldName", ex.getFieldName());
        problem.setProperty("currentState", ex.getCurrentState());
        return problem;
    }

    @ExceptionHandler({VersionConflictException.class, OptimisticLockingFailureException.class})
    ProblemDetail handleVersionConflict(RuntimeException ex, WebRequest request) {
        ProblemDetail problem = buildProblemDetail(HttpStatus.CONFLICT, ex.getMessage(), "Version Conflict", CONFLICT_TYPE, request);
        problem.setProperty("errorCode", "VERSION_CONFLICT");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), "Validation Error", VALIDATION_ERROR_TYPE, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String detail = "Invalid value for parameter '%s'. Expected type: %s.".formatted(ex.getName(), requiredType);

        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, detail, "Invalid Parameter", INVALID_PARAMETER_TYPE, request);
        problem.setProperty(PARAMETER_KEY, ex.getName());
        problem.setProperty("expectedType", requiredType);
        problem.setProperty("rejectedValue", ex.getValue());
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        ConstraintViolation<?> violation = ex.getConstraintViolations().stream().findFirst().orElse(null);
        String parameterName = PARAMETER_KEY;
        Object rejectedValue = null;

        if (violation != null) {
            String path = violation.getPropertyPath().toString();
            parameterName = path.substring(path.lastIndexOf('.') + 1);
            rejectedValue = violation.getInvalidValue();
        }

        String detail = "Invalid value for parameter '%s'.".formatted(parameterName);
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, detail, "Invalid Parameter", INVALID_PARAMETER_TYPE, request);
        problem.setProperty(PARAMETER_KEY, parameterName);
        problem.setProperty("rejectedValue", rejectedValue);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleAll(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "Internal Server Error", INTERNAL_ERROR_TYPE, request);
    }

    private ProblemDetail buildProblemDetail(HttpStatusCode status, String detail, String title, URI type, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(type);
        if (request instanceof ServletWebRequest servletWebRequest) {
            problem.setInstance(URI.create(servletWebRequest.getRequest().getRequestURI()));
        }
        return problem;
    }
}
