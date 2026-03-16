package com.mawgod.e_commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised exception → HTTP response mapping.
 *
 * Every handler returns the same {@link ErrorResponse} envelope so clients
 * always know what shape to expect:
 * <pre>
 * {
 *   "status"      : 404,
 *   "error"       : "Not Found",
 *   "message"     : "Product not found with id: '99'",
 *   "timestamp"   : "2025-03-16T12:00:00",
 *   "fieldErrors" : null          // present only for 400 validation failures
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================================================================== //
    //  Error envelope                                                      //
    // ================================================================== //

    public record ErrorResponse(
            int                 status,
            String              error,
            String              message,
            LocalDateTime       timestamp,
            Map<String, String> fieldErrors    // null unless validation error
    ) {
        static ErrorResponse of(HttpStatus status, String message) {
            return new ErrorResponse(
                    status.value(), status.getReasonPhrase(),
                    message, LocalDateTime.now(), null);
        }

        static ErrorResponse withFields(HttpStatus status, String message,
                                        Map<String, String> fieldErrors) {
            return new ErrorResponse(
                    status.value(), status.getReasonPhrase(),
                    message, LocalDateTime.now(), fieldErrors);
        }
    }

    // ================================================================== //
    //  4xx — Domain / business errors                                     //
    // ================================================================== //

    /** 404 — entity not found */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** 409 — duplicate slug, email, SKU, etc. */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** 422 — not enough stock to fulfil the request */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /** 422 — attempt to checkout with an empty cart */
    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ErrorResponse> handleEmptyCart(EmptyCartException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ================================================================== //
    //  4xx — Spring Security                                              //
    // ================================================================== //

    /**
     * 401 — bad credentials at login.
     * Note: missing / invalid JWT is handled by JwtAuthEntryPoint
     * before it reaches a controller, so this catches explicit login failures.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return response(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    /** 401 — catch-all for other Spring Security AuthenticationExceptions */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return response(HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage());
    }

    /** 401 — account disabled */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        return response(HttpStatus.UNAUTHORIZED, "Account is disabled");
    }

    /** 401 — account locked */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        return response(HttpStatus.UNAUTHORIZED, "Account is locked");
    }

    /**
     * 403 — authenticated but not authorised (e.g. CUSTOMER trying an ADMIN endpoint).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return response(HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action");
    }

    // ================================================================== //
    //  4xx — Validation & input                                           //
    // ================================================================== //

    /**
     * 400 — @Valid on a @RequestBody failed.
     * Returns each failing field + its message in fieldErrors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (first, second) -> first,   // keep first error per field
                        LinkedHashMap::new
                ));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.withFields(
                        HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors));
    }

    /**
     * 400 — path variable or request parameter has the wrong type,
     * e.g. /products/abc when id must be Long.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = String.format("Parameter '%s' has an invalid value: '%s'",
                ex.getName(), ex.getValue());
        return response(HttpStatus.BAD_REQUEST, msg);
    }

    /** 400 — required @RequestParam is absent */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return response(HttpStatus.BAD_REQUEST,
                "Required parameter '" + ex.getParameterName() + "' is missing");
    }

    /** 400 — required @RequestHeader is absent */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        return response(HttpStatus.BAD_REQUEST,
                "Required header '" + ex.getHeaderName() + "' is missing");
    }

    /**
     * 400 — malformed JSON body (bad syntax, wrong type, unrecognised enum value, etc.).
     * Gives a human-readable message without leaking internal details.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        String msg = "Malformed or unreadable request body";
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            // Surface just the first line — enough to be useful without stack details
            String firstLine = cause.getMessage().split("\n")[0];
            if (firstLine.length() <= 200) {
                msg = "Malformed request body: " + firstLine;
            }
        }
        return response(HttpStatus.BAD_REQUEST, msg);
    }

    /** 400 — generic illegal argument from service layer */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** 415 — unsupported media type */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content type '" + ex.getContentType() + "' is not supported. Use application/json");
    }

    /** 404 — no handler found for the path (Spring 6 throws NoResourceFoundException) */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return response(HttpStatus.NOT_FOUND,
                "No endpoint found for " + ex.getHttpMethod() + " " + ex.getResourcePath());
    }

    // ================================================================== //
    //  5xx                                                                //
    // ================================================================== //

    /** 422 — illegal state in service logic (e.g. guest trying to checkout) */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /**
     * 500 — safety net for any unhandled exception.
     * Clients receive a generic message so internal details are never leaked.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

    // ================================================================== //
    //  Private helper                                                      //
    // ================================================================== //

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }
}
