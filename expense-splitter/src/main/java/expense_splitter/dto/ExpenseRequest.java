package expense_splitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExpenseRequest {

    @NotBlank(message = "Description is required")
    private String description;
    // what was the expense for e.g. "Hotel"

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    // how much was spent e.g. 1200.00
    // @Positive ensures amount is always > 0

    @NotNull(message = "Member IDs are required")
    private List<Long> memberIds;
    // list of user IDs to split expense among
    // client decides who to split with
    // example: [1, 2, 3]
}