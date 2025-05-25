package dk.jk.expenses.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "tree_node", indexes = {
        @Index(name = "parent_id_ix", columnList = "parent_id")
})
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
    private List<TreeNode> children = new ArrayList<>();

    @OneToMany(mappedBy = "treeNode")
    private List<Posting> postings = new ArrayList<>();

    @ManyToOne
    private Label label;

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
}