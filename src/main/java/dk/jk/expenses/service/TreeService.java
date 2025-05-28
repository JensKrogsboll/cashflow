package dk.jk.expenses.service;

import dk.jk.expenses.entity.Label;
import dk.jk.expenses.entity.TreeNode;
import dk.jk.expenses.repository.LabelRepository;
import dk.jk.expenses.repository.TreeNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TreeService {

    private final TreeNodeRepository treeNodeRepository;
    private final LabelRepository labelRepository;
    private final Pattern segmentsToIgnore = Pattern.compile("^[0-9]+$");

    public TreeNode buildOrFindPath(String[] segments) {
        TreeNode parent = null;
        List<String> filteredSegments = segments.length == 1 ?
                Arrays.asList(segments) :
                Arrays.stream(segments)
                        .filter(segment -> !segmentsToIgnore.matcher(segment).matches())
                        .toList();
        for (String segment : filteredSegments) {
            final TreeNode par = parent;  // final or effectively final copy

            TreeNode node = treeNodeRepository.findAll().stream()
                    .filter(n -> n.getName().equalsIgnoreCase(segment)
                            && ((n.getParent() == null && par == null)
                            || (n.getParent() != null && par != null
                            && n.getParent().getId().equals(par.getId()))))
                    .findFirst()
                    .orElse(null);

            if (node == null) {
                node = new TreeNode();
                node.setName(segment);
                node.setParent(par);
                node = treeNodeRepository.save(node);
                if (par == null) {
                    setLabel(node.getId(), "Diverse");
                }
            }

            // Now we can reassign 'parent' safely outside the lambda
            parent = node;
        }
        return parent;
    }

    public List<TreeNode> getFullTree() {
        return treeNodeRepository.findAll();
    }

    public List<TreeNode> getNodes(Long parentId) {
        if (parentId == null) {
            return treeNodeRepository.findByParentIdIsNull();
        }
        return treeNodeRepository.findByParentId(parentId);
    }

    public TreeNode setLabel(Long nodeId, String labelName) {
        TreeNode node = treeNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        Label label = labelRepository.findByName(labelName);
        if (label == null) {
            label = new Label();
            label.setName(labelName);
            labelRepository.save(label);
        }
        TreeNode labelParent = node.findLabelNode(node.getParent());
        if (labelParent != node && label.equals(labelParent.getLabel())) {
            node.setLabel(null);
        } else {
            node.setLabel(label);
        }
        return treeNodeRepository.save(node);
    }

    public TreeNode clearLabel(Long nodeId) {
        TreeNode node = treeNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        node.setLabel(null);
        treeNodeRepository.save(node);
        return node;
    }
    
    public List<String> listAllLabels() {
        return labelRepository.findAll().stream().map(Label::getName).collect(Collectors.toList());
    }

    public void updateLabelsFromMap(Map<String, String> map) {
        treeNodeRepository.findAll().stream()
                .filter(n -> map.containsKey(n.getPath()))
                .forEach(n -> setLabel(n.getId(), map.get(n.getPath())));
    }
}
