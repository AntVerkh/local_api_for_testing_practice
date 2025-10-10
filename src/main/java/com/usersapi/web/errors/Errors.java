package com.usersapi.web.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class Errors {

    private static final Logger log = LoggerFactory.getLogger(Errors.class);

    // 400
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String msg) { super(msg); }
    }

    // 404
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

    // 405
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public static class MethodNotAllowedException extends RuntimeException {
        public MethodNotAllowedException(String msg) { super(msg); }
    }

    // 406
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public static class NotAcceptableException extends RuntimeException {
        public NotAcceptableException(String message) { super(message); }
    }

    // 409
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }

    // 413
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public static class PayloadTooLargeException extends RuntimeException {
        public PayloadTooLargeException(String message) { super(message); }
    }

    // 415
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public static class UnsupportedMediaTypeException extends RuntimeException {
        public UnsupportedMediaTypeException(String message) { super(message); }
    }

    // 418
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public static class TeapotException extends RuntimeException {
        public TeapotException(String message) { super(message); }
    }

    // 422
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class UnprocessableEntityException extends RuntimeException {
        public UnprocessableEntityException(String message) { super(message); }
    }

    // 423
    @ResponseStatus(HttpStatus.LOCKED)
    public static class LockedException extends RuntimeException {
        public LockedException(String message) { super(message); }
    }

    // 431
    @ResponseStatus(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)
    public static class HeaderTooLargeException extends RuntimeException {
        public HeaderTooLargeException(String message) { super(message); }
    }

    // 500
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class InternalErrorException extends RuntimeException {
        public InternalErrorException(String message) { super(message); }
    }

    // Обработчики исключений

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse("VALIDATION_ERROR", "Validation failed");
        errorResponse.setDetails(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException badRequestException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", badRequestException.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException notFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", notFoundException.getMessage()));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(MethodNotAllowedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse("METHOD_NOT_ALLOWED", exception.getMessage()));
    }

    @ExceptionHandler(NotAcceptableException.class)
    public ResponseEntity<?> handleNotAcceptable(NotAcceptableException exception) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(new ErrorResponse("NOT_ACCEPTABLE", exception.getMessage()));
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConflictException.class})
    public ResponseEntity<?> handleConflict(Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", exception.getMessage()));
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, PayloadTooLargeException.class})
    public ResponseEntity<?> handlePayloadTooLarge() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse("PAYLOAD_TOO_LARGE", "File size exceeds maximum allowed"));
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class, UnsupportedMediaTypeException.class})
    public ResponseEntity<?> handleUnsupportedMediaType(Exception exception) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponse("UNSUPPORTED_MEDIA_TYPE", exception.getMessage()));
    }

    @ExceptionHandler(TeapotException.class)
    public ResponseEntity<?> handleTeapot(TeapotException exception) {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
                .body(new ErrorResponse("I_AM_A_TEAPOT", exception.getMessage()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, UnprocessableEntityException.class})
    public ResponseEntity<?> handleUnprocessableEntity(Exception exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("UNPROCESSABLE_ENTITY", exception.getMessage()));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLocked(LockedException lockedException) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(new ErrorResponse("LOCKED", lockedException.getMessage()));
    }

    @ExceptionHandler(HeaderTooLargeException.class)
    public ResponseEntity<?> handleHeaderTooLarge(HeaderTooLargeException exception) {
        return ResponseEntity.status(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)
                .body(new ErrorResponse("REQUEST_HEADER_FIELDS_TOO_LARGE", exception.getMessage()));
    }

    @ExceptionHandler({InternalErrorException.class, Exception.class})
    public ResponseEntity<?> handleInternal(Exception exception) {
        if (exception instanceof InternalErrorException) {
            log.error("Internal error: {}", exception.getMessage(), exception);
        } else {
            log.error("Unhandled error: {}", exception.getMessage(), exception);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse("METHOD_NOT_ALLOWED", exception.getMessage()));
    }

    public static class ErrorResponse {
        private String code;
        private String message;
        private Map<String, String> details;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Map<String, String> getDetails() { return details; }
        public void setDetails(Map<String, String> details) { this.details = details; }
    }
}