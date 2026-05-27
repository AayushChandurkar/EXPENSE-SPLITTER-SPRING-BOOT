package expense_splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberShareResponse {

    private Long userId;
    private String userName;
    // who owes this share
    private BigDecimal shareAmount;
    // how much they owe
    private boolean settled;
    // have they paid it back?
}