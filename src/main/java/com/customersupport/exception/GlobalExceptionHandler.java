// package com.customersupport.exception;
//
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.validation.FieldError;
// import org.springframework.web.ErrorResponse;
// import org.springframework.web.bind.MethodArgumentNotValidException;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;
//
// import java.time.LocalDateTime;
// import java.util.HashMap;
// import java.util.LinkedHashMap;
// import java.util.Map;
//
// @ControllerAdvice
// public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException
// ex){
//        ErrorResponse errorResponse = new com.customersupport.dto.ErrorResponse(
//                LocalDateTime.now(),
//                HttpStatus.NOT_FOUND.value(),
//                "Resource Not Found",
//                ex.getMessage()
//        );
//        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(UnauthorizedException.class)
//    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex){
//        ErrorResponse errorResponse = new com.customersupport.dto.ErrorResponse(
//                LocalDateTime.now(),
//                HttpStatus.UNAUTHORIZED.value(),
//                "Unauthorized",
//                ex.getMessage()
//        );
//        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, Object>>
// handleValidationExceptions(MethodArgumentNotValidException ex){
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("timestamp", LocalDateTime.now());
//        response.put("status", HttpStatus.BAD_REQUEST.value());
//        response.put("error", "Validation Error");
//        response.put("errors", errors);
//
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleException(Exception ex){
//        ErrorResponse errorResponse = new com.customersupport.dto.ErrorResponse(
//                LocalDateTime.now(),
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                "Internal Server Error",
//                ex.getMessage()
//        );
//        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//
//    }
// }

package com.customersupport.exception;

import com.customersupport.dto.ErrorResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Validation Error");
    response.put("errors", errors);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
