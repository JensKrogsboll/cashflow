package dk.jk.expenses.repository;

import dk.jk.expenses.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
    Label findByName(String name);
}
