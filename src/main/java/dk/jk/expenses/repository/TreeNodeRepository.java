package dk.jk.expenses.repository;

import dk.jk.expenses.entity.TreeNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreeNodeRepository extends JpaRepository<TreeNode, Long> {
    List<TreeNode> findByParentId(Long parentId);

    List<TreeNode> findByParentIdIsNull();
}
