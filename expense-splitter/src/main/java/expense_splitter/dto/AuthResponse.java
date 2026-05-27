package expense_splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// generates constructor with all fields
// we need this to create AuthResponse(token) easily
public class AuthResponse {

    private String token;
    // the JWT token sent back to client after
    // successful register or login
}