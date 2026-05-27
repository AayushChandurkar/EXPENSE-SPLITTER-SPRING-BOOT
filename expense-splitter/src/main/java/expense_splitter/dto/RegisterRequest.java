package expense_splitter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
// Lombok generates getters, setters, toString automatically
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    // field cannot be null or empty string
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    // must be valid email format and cannot be empty
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank(message = "Password is required")
    // minimum 6 characters, cannot be empty
    private String password;
}