package com.devices.api;

import com.devices.service.exception.DeviceNotFoundException;
import com.devices.service.exception.InvalidDeviceStateException;
import com.devices.service.exception.DeviceInUseException;
import com.devices.service.exception.ImmutableFieldViolationException;
import com.devices.service.exception.DeviceFieldLockedException;
import com.devices.service.exception.VersionConflictException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String VALIDATION_PROBLEM_TYPE = "https://api.example.com/errors/validation-error";
    private static final String MALFORMED_JSON_PROBLEM_TYPE = "https://api.example.com/errors/malformed-json";
    private static final String NOT_FOUND_PROBLEM_TYPE = "https://api.example.com/errors/device-not-found";
    private static final String INVALID_STATE_PROBLEM_TYPE = "https://api.example.com/errors/invalid-device-state";
    private static final String INVALID_PARAMETER_PROBLEM_TYPE = "https://api.example.com/errors/invalid-parameter";
    private static final String DEVICE_IN_USE_PROBLEM_TYPE = "https://api.example.com/errors/device-in-use";
    private static final String UNPROCESSABLE_PROBLEM_TYPE = "https://api.example.com/errors/unprocessable-entity";
    private static final String VERSION_CONFLICT_PROBLEM_TYPE = "https://api.example.com/errors/version-conflict";
    private static final String INTERNAL_ERROR_PROBLEM_TYPE = "https://api.example.com/errors/internal-server-error";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                    HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setTitle("Validation Error");
        problem.setType(URI.create(VALIDATION_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(
                "errors",
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(err -> Map.of(
                                "field", err.getField(),
                                "message", err.getDefaultMessage(),
                                "rejectedValue", err.getRejectedValue()))
                        .collect(Collectors.toList())
        );

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                      HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed JSON request");
        problem.setTitle("Malformed Request");
        problem.setType(URI.create(MALFORMED_JSON_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleDeviceNotFound(DeviceNotFoundException ex,
                                                              HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Device Not Found");
        problem.setType(URI.create(NOT_FOUND_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidDeviceStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDeviceState(InvalidDeviceStateException ex,
                                                                  HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Device State");
        problem.setType(URI.create(INVALID_STATE_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(DeviceInUseException.class)
    public ResponseEntity<ProblemDetail> handleDeviceInUse(DeviceInUseException ex,
                                                           HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Device In Use");
        problem.setType(URI.create(DEVICE_IN_USE_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(ImmutableFieldViolationException.class)
    public ResponseEntity<ProblemDetail> handleImmutableField(ImmutableFieldViolationException ex,
                                                              HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Unprocessable Entity");
        problem.setType(URI.create(UNPROCESSABLE_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "IMMUTABLE_FIELD_VIOLATION");
        problem.setProperty("fieldName", ex.getFieldName());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(DeviceFieldLockedException.class)
    public ResponseEntity<ProblemDetail> handleFieldLocked(DeviceFieldLockedException ex,
                                                           HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Unprocessable Entity");
        problem.setType(URI.create(UNPROCESSABLE_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "DEVICE_IN_USE_FIELD_LOCKED");
        problem.setProperty("fieldName", ex.getFieldName());
        problem.setProperty("currentState", ex.getCurrentState());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler({VersionConflictException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ProblemDetail> handleVersionConflict(RuntimeException ex,
                                                               HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Version Conflict");
        problem.setType(URI.create(VERSION_CONFLICT_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "VERSION_CONFLICT");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Validation Error");
        problem.setType(URI.create(VALIDATION_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create(INTERNAL_ERROR_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String parameterName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String detail = ("Invalid value for parameter '%s'. Expected type: %s.").formatted(parameterName, requiredType);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Invalid Parameter");
        problem.setType(URI.create(INVALID_PARAMETER_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("parameter", parameterName);
        problem.setProperty("expectedType", requiredType);
        problem.setProperty("rejectedValue", ex.getValue());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        // Take first violation to build a concise RFC7807 problem
        ConstraintViolation<?> violation = ex.getConstraintViolations().stream().findFirst().orElse(null);
        String parameterName = "parameter";
        Object rejectedValue = null;
        if (violation != null) {
            String path = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "";
            // Extract last node name (e.g., getByBrand.brand -> brand)
            int idx = path.lastIndexOf('.');
            parameterName = idx >= 0 ? path.substring(idx + 1) : path;
            rejectedValue = violation.getInvalidValue();
        }

        String expectedType = "String"; // for @RequestParam validations in this API
        String detail = ("Invalid value for parameter '%s'. Expected type: %s.").formatted(parameterName, expectedType);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Invalid Parameter");
        problem.setType(URI.create(INVALID_PARAMETER_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("parameter", parameterName);
        problem.setProperty("expectedType", expectedType);
        problem.setProperty("rejectedValue", rejectedValue);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingRequestParameter(MissingServletRequestParameterException ex,
                                                                       HttpServletRequest request) {
        String parameterName = ex.getParameterName();
        String expectedType = ex.getParameterType() != null ? ex.getParameterType() : "String";
        String detail = ("Missing required parameter '%s'. Expected type: %s.").formatted(parameterName, expectedType);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Invalid Parameter");
        problem.setType(URI.create(INVALID_PARAMETER_PROBLEM_TYPE));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("parameter", parameterName);
        problem.setProperty("expectedType", expectedType);
        return ResponseEntity.badRequest().body(problem);
    }
}
