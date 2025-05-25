package dk.jk.expenses.controller;

import dk.jk.expenses.entity.TreeNode;
import dk.jk.expenses.service.TreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/tree")
@RequiredArgsConstructor
public class TreeController {

    private final TreeService treeService;

    @GetMapping
    public List<TreeNode> getTree(@RequestParam(name = "parentId", required = false) Long parentId) {
        return treeService.getNodes(parentId).stream()
                .sorted(Comparator.comparing(TreeNode::getId))
                .toList();
    }

    @PostMapping("/{nodeId}/label/{label}")
    public TreeNode setLabel(@PathVariable Long nodeId, @PathVariable String label) {
        return treeService.setLabel(nodeId, label);
    }

    @DeleteMapping("/{nodeId}/label")
    public TreeNode clearLabel(@PathVariable Long nodeId) {
        return treeService.clearLabel(nodeId);
    }
}

record LabelRequest(String labelName) {}
