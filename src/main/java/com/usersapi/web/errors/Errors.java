package com.usersapi.web.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class Errors {

    private static final Logger log = LoggerFactory.getLogger(Errors.class);

    // 404
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String msg) { super(msg); }
    }

    // 405
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public static class MethodNotAllowedException extends RuntimeException {
        public MethodNotAllowedException(String msg) { super(msg); }
    }

    // 406
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public static class NotAcceptableException extends RuntimeException {
        public NotAcceptableException(String msg) { super(msg); }
    }

    // 409
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class ConflictException extends RuntimeException {
        public ConflictException(String msg) { super(msg); }
    }

    // 413
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public static class PayloadTooLargeException extends RuntimeException {
        public PayloadTooLargeException(String msg) { super(msg); }
    }

    // 415
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public static class UnsupportedMediaTypeException extends RuntimeException {
        public UnsupportedMediaTypeException(String msg) { super(msg); }
    }

    // 418
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public static class TeapotException extends RuntimeException {
        public TeapotException(String msg) { super(msg); }
    }

    // 422
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class UnprocessableEntityException extends RuntimeException {
        public UnprocessableEntityException(String msg) { super(msg); }
    }

    // 423
    @ResponseStatus(HttpStatus.LOCKED)
    public static class LockedException extends RuntimeException {
        public LockedException(String msg) { super(msg); }
    }

    // 431
    @ResponseStatus(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)
    public static class HeaderTooLargeException extends RuntimeException {
        public HeaderTooLargeException(String msg) { super(msg); }
    }

    // 500
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class InternalErrorException extends RuntimeException {
        public InternalErrorException(String msg) { super(msg); }
    }

    // Обработчики исключений

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(MethodNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse("METHOD_NOT_ALLOWED", ex.getMessage()));
    }

    @ExceptionHandler(NotAcceptableException.class)
    public ResponseEntity<?> handleNotAcceptable(NotAcceptableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(new ErrorResponse("NOT_ACCEPTABLE", ex.getMessage()));
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConflictException.class})
    public ResponseEntity<?> handleConflict(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, PayloadTooLargeException.class})
    public ResponseEntity<?> handlePayloadTooLarge(Exception ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse("PAYLOAD_TOO_LARGE", "File size exceeds maximum allowed"));
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class, UnsupportedMediaTypeException.class})
    public ResponseEntity<?> handleUnsupportedMediaType(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponse("UNSUPPORTED_MEDIA_TYPE", ex.getMessage()));
    }

    @ExceptionHandler(TeapotException.class)
    public ResponseEntity<?> handleTeapot(TeapotException ex) {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
                .body(new ErrorResponse("I_AM_A_TEAPOT", ex.getMessage()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, UnprocessableEntityException.class})
    public ResponseEntity<?> handleUnprocessableEntity(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("UNPROCESSABLE_ENTITY", ex.getMessage()));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLocked(LockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(new ErrorResponse("LOCKED", ex.getMessage()));
    }

    @ExceptionHandler(HeaderTooLargeException.class)
    public ResponseEntity<?> handleHeaderTooLarge(HeaderTooLargeException ex) {
        return ResponseEntity.status(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)
                .body(new ErrorResponse("REQUEST_HEADER_FIELDS_TOO_LARGE", ex.getMessage()));
    }

    @ExceptionHandler({InternalErrorException.class, Exception.class})
    public ResponseEntity<?> handleInternal(Exception ex) {
        if (ex instanceof InternalErrorException) {
            log.error("Internal error: {}", ex.getMessage(), ex);
        } else {
            log.error("Unhandled error: {}", ex.getMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An internal error occurred"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse("METHOD_NOT_ALLOWED", ex.getMessage()));
    }

    public static class ErrorResponse {
        private String code;
        private String message;
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
}