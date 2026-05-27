package expense_splitter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
// combination of @ControllerAdvice + @ResponseBody
// intercepts ALL exceptions from ALL controllers
// returns clean JSON error responses automatically
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    // handles our custom exception — when resource not found
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("status", 404);
        // HTTP 404 = Not Found
        error.put("message", ex.getMessage());
        // the message we passed when throwing the exception

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        // returns 404 response with error details
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // handles validation failures — when @Valid finds invalid input
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            // which field failed validation e.g. "email"
            String message = error.getDefaultMessage();
            // the message we wrote e.g. "Invalid email format"
            errors.put(fieldName, message);
        });
        // collects ALL validation errors at once
        // example: { "email": "Invalid email format",
        //             "password": "Password must be at least 6 characters" }

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        // HTTP 400 = Bad Request
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    // catches any other runtime exception we didn't specifically handle
    public ResponseEntity<Map<String, Object>> handleRuntime(
            RuntimeException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("status", 400);
        error.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    // catches absolutely everything else — last resort
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("status", 500);
        // HTTP 500 = Internal Server Error
        error.put("message", "Something went wrong");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}