package dk.jk.expenses.controller;

import dk.jk.expenses.entity.Label;
import dk.jk.expenses.entity.TreeNode;
import dk.jk.expenses.repository.LabelRepository;
import dk.jk.expenses.service.TreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/tree")
@RequiredArgsConstructor
public class TreeController {

    private final TreeService treeService;
    private final LabelRepository labelRepository;

    @GetMapping
    public List<TreeNode> getTree(@RequestParam(name = "parentId", required = false) Long parentId,
                                  @RequestParam(name = "label", required = false) String label) {
        if (label == null) {
            return getTreeNodes(parentId);
        }
        return getTreeNodes(label);
    }

    private List<TreeNode> getTreeNodes(String label) {
        return treeService.getFullTree().stream()
                .filter(n -> n.getLabel() != null && label.equals(n.getLabel().getName()))
                .sorted(Comparator.comparing(TreeNode::getName))
                .toList();
    }

    private List<TreeNode> getTreeNodes(Long parentId) {
        var rootNodes = treeService.getNodes(parentId).stream()
                .sorted(Comparator.comparing(treeNode -> {
                    var label = treeNode.getEffectiveLabel().getName();
                    return label != null ? label + treeNode.getName() : treeNode.getName();
                }));
        if (parentId == null) {
            var labelNodes = labelRepository.findAll().stream()
                    .sorted(Comparator.comparing(Label::getName))
                    .map(label -> TreeNode.builder().name(label.getName()).build());
            return Stream.concat(labelNodes, rootNodes
                    .filter(n -> n.getLabel() == null || "default".equals(n.getLabel().getName()))
            ).toList();
        }
        return rootNodes.toList();
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
