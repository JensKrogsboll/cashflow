package dk.jk.expenses.controller;

import dk.jk.expenses.entity.Posting;
import dk.jk.expenses.entity.TreeNode;
import dk.jk.expenses.repository.TreeNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/posting")
@RequiredArgsConstructor
public class PostingController {

    private final TreeNodeRepository nodeRepository;

    @GetMapping
    public Collection<Posting> getPostings(@RequestParam(name = "treeNodeId", required = false) Long treeNodeId) {
        Optional<TreeNode> treeNode = nodeRepository.findById(treeNodeId);
        return treeNode.<Collection<Posting>>map(node -> getChildren(node)
                .flatMap(n -> n.getPostings().stream())
                .toList()).orElseGet(ArrayList::new);
    }

    private Stream<TreeNode> getChildren(TreeNode node) {
        var children = node.getChildren();
        if (children.isEmpty()) {
            return Stream.of(node);
        }
        return children.stream().flatMap(n -> Stream.concat(Stream.of(node), getChildren(n)));
    }

}
