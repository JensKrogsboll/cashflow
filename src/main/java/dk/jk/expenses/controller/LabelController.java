package dk.jk.expenses.controller;

import dk.jk.expenses.entity.TreeNode;
import dk.jk.expenses.service.TreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/label")
@RequiredArgsConstructor
public class LabelController {

    private final TreeService treeService;

    @GetMapping
    public Collection<String> getLabels() {
        return treeService.listAllLabels().stream().sorted().toList();
    }

    @GetMapping("map")
    public Map<String, String> getNodeToLabelMap() {
        return treeService.getFullTree().stream()
                .filter(n -> n.getLabel() != null)
                .collect(Collectors.toMap(TreeNode::getPath, n -> n.getLabel().getName()));
    }

    @PostMapping("map")
    public String putNodeToLabelMap(@RequestBody Map<String, String> map) {
        treeService.updateLabelsFromMap(map);
        return "success";
    }
}
