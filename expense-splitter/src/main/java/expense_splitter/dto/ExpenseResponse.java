package expense_splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private String paidBy;
    // just the name of who paid — not entire User object
    private List<MemberShareResponse> memberShares;
    // list of each person's share details
    private LocalDateTime createdAt;
}