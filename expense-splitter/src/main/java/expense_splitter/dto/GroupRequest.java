package expense_splitter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupRequest {

    @NotBlank(message = "Group name is required")
    private String name;
    // group name e.g. "Goa Trip"

    private String description;
    // optional — no validation needed
}