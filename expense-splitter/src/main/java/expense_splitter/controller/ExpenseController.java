package expense_splitter.controller;

import expense_splitter.dto.ExpenseRequest;
import expense_splitter.dto.ExpenseResponse;
import expense_splitter.dto.MemberShareResponse;
import expense_splitter.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
// base path is /api — not /api/expenses
// because some endpoints are under /groups/{id}/expenses
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/groups/{groupId}/expenses")
    // POST /api/groups/1/expenses → add expense to group 1
    public ResponseEntity<ExpenseResponse> addExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody ExpenseRequest request) {

        ExpenseResponse response =
                expenseService.addExpense(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // 201 CREATED — new expense created
    }

    @GetMapping("/groups/{groupId}/expenses")
    // GET /api/groups/1/expenses → get all expenses of group 1
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
            @PathVariable Long groupId) {

        return ResponseEntity.ok(
                expenseService.getGroupExpenses(groupId));
    }

    @GetMapping("/expenses/{expenseId}")
    // GET /api/expenses/1 → get one specific expense
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable Long expenseId) {

        return ResponseEntity.ok(
                expenseService.getExpenseById(expenseId));
    }

    @DeleteMapping("/expenses/{expenseId}")
    // DELETE /api/expenses/1 → delete expense
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long expenseId) {

        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
        // 204 No Content — successful deletion
        // nothing to return after delete
    }

    @PostMapping("/expenses/{expenseId}/settle")
    // POST /api/expenses/1/settle → settle your share
    public ResponseEntity<MemberShareResponse> settleExpense(
            @PathVariable Long expenseId) {

        return ResponseEntity.ok(
                expenseService.settleExpense(expenseId));
    }
}