package dk.jk.expenses.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "posting", indexes = {
        @Index(name = "tree_node_id_ix", columnList = "tree_node_id")
})

public class Posting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private Integer sequenceNumber;

    @Column(length = 1024)
    private String text;

    private BigDecimal amount;

    @ManyToOne
    @JsonIgnore
    private TreeNode treeNode;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Posting posting = (Posting) o;
        return Objects.equals(date, posting.date) && Objects.equals(text, posting.text) && Objects.equals(amount, posting.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, text, amount);
    }
}