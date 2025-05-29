package dk.jk.expenses.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.tree.Tree;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Getter
@Setter
@Table(name = "tree_node", indexes = {
        @Index(name = "parent_id_ix", columnList = "parent_id")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JsonIgnore
    private TreeNode parent;

    @OneToMany(mappedBy = "parent")
    @JsonIgnore
    private List<TreeNode> children;

    @OneToMany(mappedBy = "treeNode")
    private List<Posting> postings;

    @ManyToOne
    private Label label;

    public String getPath() {
        return getPath(this);
    }

    private String getPath(TreeNode node) {
        return "/" + getPathAsStream(node).map(TreeNode::getName).collect(Collectors.joining("/"));
    }

    private Stream<TreeNode> getPathAsStream(TreeNode node) {
        if (node.getParent() == null) {
            return Stream.of(node);
        }
        return Stream.concat(getPathAsStream(node.getParent()), Stream.of(node));
    }

    public Label getEffectiveLabel() {
        return label != null ? label : findLabelNode(this).label;
    }

    public TreeNode findLabelNode(TreeNode node) {
        if (node == null) {
            return this;
        }
        if (node.getLabel() != null) {
            return node;
        }
        var parent = node.getParent();
        if (parent == null) {
            return node;
        }
        if (parent.getLabel() != null) {
            return parent;
        }
        return findLabelNode(parent);
    }

    public BigDecimal getSum() {
        var children = getChildren();
        if (children == null) {
            return BigDecimal.ZERO;
        }
        return this.getChildren().stream()
                .filter(t -> t.getEffectiveLabel() != null && t.getEffectiveLabel().equals(this.getEffectiveLabel()))
                .map(TreeNode::getSum)
                .reduce(this.getPostings().stream()
                        .map(Posting::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add), BigDecimal::add);
    }
}