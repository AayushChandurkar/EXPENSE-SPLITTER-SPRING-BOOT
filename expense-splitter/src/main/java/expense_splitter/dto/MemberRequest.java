package expense_splitter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;
    // id of user to add to group
}