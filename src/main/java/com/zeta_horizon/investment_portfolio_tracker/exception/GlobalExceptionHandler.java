package com.zeta_horizon.investment_portfolio_tracker.exception;

import com.zeta_horizon.investment_portfolio_tracker.dto.ErrorResponse;
import com.zeta_horizon.investment_portfolio_tracker.dto.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setTimestamp(LocalDateTime.now());

        ex.getBindingResult().getFieldErrors().forEach(error ->
                response.addError(error.getField(), error.getDefaultMessage()));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException exception
            , HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SupportTicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSupportTicketNotFoundException(SupportTicketNotFoundException exception
            ,HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvestmentProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvesmentProductNotFoundException(
            InvestmentProductNotFoundException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception, HttpServletRequest request) {
        String errors = exception.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentExceptions(RuntimeException exception
            ,HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TicketClosedException.class)
    public ResponseEntity<ErrorResponse> handleClosedTicketModificationExceptions(TicketClosedException exception
            ,HttpServletRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();
    }
}

