package expense_splitter.exception;

public class ResourceNotFoundException extends RuntimeException {
    // extends RuntimeException — makes this an unchecked exception
    // unchecked means — you don't have to declare it with throws
    // just throw it anywhere and GlobalExceptionHandler catches it

    public ResourceNotFoundException(String message) {
        super(message);
        // passes message to RuntimeException
        // message is what gets shown in the error response
        // example: "User not found with email: aayush@gmail.com"
    }
}