package expense_splitter.service;
import expense_splitter.entity.Expense;
import expense_splitter.dto.ExpenseRequest;
import expense_splitter.dto.ExpenseResponse;
import expense_splitter.dto.MemberShareResponse;
import expense_splitter.entity.ExpenseMember;
import expense_splitter.entity.Group;
import expense_splitter.entity.User;
import expense_splitter.exception.ResourceNotFoundException;
import expense_splitter.repository.ExpenseMemberRepository;
import expense_splitter.repository.ExpenseRepository;
import expense_splitter.repository.GroupRepository;
import expense_splitter.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseMemberRepository expenseMemberRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          GroupRepository groupRepository,
                          UserRepository userRepository,
                          ExpenseMemberRepository expenseMemberRepository) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.expenseMemberRepository = expenseMemberRepository;
    }

    // gets currently logged in user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    // converts ExpenseMember to MemberShareResponse DTO
    private MemberShareResponse mapToMemberShare(ExpenseMember em) {
        return MemberShareResponse.builder()
                .userId(em.getUser().getId())
                .userName(em.getUser().getName())
                .shareAmount(em.getShareAmount())
                .settled(em.isSettled())
                .build();
    }

    // converts Expense entity to ExpenseResponse DTO
    private ExpenseResponse mapToResponse(Expense expense) {
        List<MemberShareResponse> shares = expense
                .getExpenseMembers()
                .stream()
                .map(this::mapToMemberShare)
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .paidBy(expense.getPaidBy().getName())
                .memberShares(shares)
                .createdAt(expense.getCreatedAt())
                .build();
    }

    @Transactional
    public ExpenseResponse addExpense(Long groupId,
                                      ExpenseRequest request) {

        // Step 1 — get current user (who is paying)
        User currentUser = getCurrentUser();

        // Step 2 — find the group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: " + groupId));

        // Step 3 — build Expense entity
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .group(group)
                .paidBy(currentUser)
                // currently logged in user paid
                .build();

        // Step 4 — calculate split
        int memberCount = request.getMemberIds().size();
        // how many people are splitting

        BigDecimal shareAmount = request.getAmount()
                .divide(new BigDecimal(memberCount), 2, RoundingMode.HALF_UP);
        // divide total amount by number of members
        // scale = 2 means 2 decimal places e.g. 400.00
        // RoundingMode.HALF_UP — standard rounding
        // 33.335 → 33.34

        // Step 5 — create ExpenseMember for each person
        List<ExpenseMember> expenseMembers = new ArrayList<>();

        for (Long memberId : request.getMemberIds()) {
            User member = userRepository.findById(memberId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with id: " + memberId));

            ExpenseMember expenseMember = ExpenseMember.builder()
                    .expense(expense)
                    .user(member)
                    .shareAmount(shareAmount)
                    // everyone gets equal share
                    .settled(false)
                    // starts as unsettled
                    .build();

            expenseMembers.add(expenseMember);
        }

        expense.setExpenseMembers(expenseMembers);
        // attach all member records to expense

        expenseRepository.save(expense);
        // CascadeType.ALL saves all ExpenseMembers automatically
        // one save → saves expense + all its members

        return mapToResponse(expense);
    }

    @Transactional
    public List<ExpenseResponse> getGroupExpenses(Long groupId) {

        // verify group exists
        groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found with id: " + groupId));

        return expenseRepository.findByGroup_Id(groupId)
                // our custom repository method
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseResponse getExpenseById(Long expenseId) {

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found with id: " + expenseId));

        return mapToResponse(expense);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found with id: " + expenseId));

        expenseRepository.delete(expense);
        // CascadeType.ALL automatically deletes
        // all ExpenseMember records too
    }

    @Transactional
    public MemberShareResponse settleExpense(Long expenseId) {

        User currentUser = getCurrentUser();
        // who is settling — the logged in user

        ExpenseMember expenseMember = expenseMemberRepository
                .findByExpense_IdAndUser_Id(expenseId, currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No share found for this expense"));
        // find THIS user's record for THIS expense

        if (expenseMember.isSettled()) {
            throw new RuntimeException("Already settled");
            // prevent double settling
        }

        expenseMember.setSettled(true);
        // flip settled flag to true
        expenseMemberRepository.save(expenseMember);

        return mapToMemberShare(expenseMember);
        // return updated share details
    }
}