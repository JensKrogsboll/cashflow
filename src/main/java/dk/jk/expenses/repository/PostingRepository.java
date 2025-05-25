package dk.jk.expenses.repository;

import dk.jk.expenses.entity.Posting;
import dk.jk.expenses.entity.TreeNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public interface PostingRepository extends JpaRepository<Posting, Long> {

    Optional<Posting> findByDateAndAmountAndTreeNodeAndSequenceNumber(LocalDate date, BigDecimal amount, TreeNode node, Integer sequenceNumber);

    Collection<Posting> findByTreeNode(TreeNode node);
}
