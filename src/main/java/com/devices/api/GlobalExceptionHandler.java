package com.devices.api;

import com.devices.service.exception.DeviceNotFoundException;
import com.devices.service.exception.InvalidDeviceStateException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final String VALIDATION_PROBLEM_TYPE = "https://api.example.com/errors/validation-error";
    private static final String MALFORMED_JSON_PROBLEM_TYPE = "https://api.example.com/errors/malformed-json";
    private static final String NOT_FOUND_PROBLEM_TYPE = "https://api.example.com/errors/device-not-found";
    private static final String INVALID_STATE_PROBLEM_TYPE = "https://api.example.com/errors/invalid-device-state";
    private static final String INVALID_PARAMETER_PROBLEM_TYPE = "https://api.example.com/errors/invalid-parameter";

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
}
