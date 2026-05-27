package expense_splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private String createdBy;
    // just the name of creator — not entire User object
    // never expose full entity in response
    private List<String> members;
    // just names of members — not entire User objects
    private LocalDateTime createdAt;
}