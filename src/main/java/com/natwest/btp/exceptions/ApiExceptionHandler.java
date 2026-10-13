package com.natwest.btp.exceptions;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

   @ExceptionHandler(BankingException.class)
   public ResponseEntity<ApiError> handleBankingException(BankingException ex, HttpServletRequest request) {
	   HttpStatus status = ex.getCode() == BankingException.Code.ACCOUNT_NOT_FOUND
			   ? HttpStatus.NOT_FOUND
			   : HttpStatus.BAD_REQUEST;
	   return ResponseEntity.status(status)
			   .body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI()));
   }
   
   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
	   FieldError error = ex.getBindingResult().getFieldError();
	   String message = error != null ? error.getDefaultMessage() : "Validation Failed";
	   return ResponseEntity.badRequest()
			   .body(new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), message, request.getRequestURI()));
   }
   
   @ExceptionHandler(ConstraintViolationException.class)
   public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
	   return ResponseEntity.badRequest()
			   .body(new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage(), request.getRequestURI()));
   }
   
   @ExceptionHandler(HttpMessageNotReadableException.class)
   public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
	   return ResponseEntity.badRequest()
			   .body(new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), "Request body is invalid or malformed", request.getRequestURI()));
   }
   
   @ExceptionHandler(Exception.class)
   public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
	   return ResponseEntity.badRequest()
			   .body(new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), "Unexpected server eror", request.getRequestURI()));
   }
   
   public record ApiError(Instant timestamp, int status, String error, String message, String path) {	
   }
}