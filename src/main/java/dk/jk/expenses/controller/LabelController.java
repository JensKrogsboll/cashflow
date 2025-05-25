package dk.jk.expenses.controller;

import dk.jk.expenses.service.TreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/label")
@RequiredArgsConstructor
public class LabelController {

    private final TreeService treeService;

    @GetMapping
    public Collection<String> getLabels() {
        return treeService.listAllLabels();
    }

}
