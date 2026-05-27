package expense_splitter.repository;

import expense_splitter.entity.ExpenseMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseMemberRepository extends JpaRepository<ExpenseMember,Long> {
    // multiple results → List
    List<ExpenseMember> findByExpense_Id(Long expenseId);
    // one result → Optional
    Optional<ExpenseMember> findByExpense_IdAndUser_Id(Long expenseId, Long userId);
}
