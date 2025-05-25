package dk.jk.expenses.controller;

import dk.jk.expenses.entity.Posting;
import dk.jk.expenses.entity.TreeNode;
import dk.jk.expenses.repository.PostingRepository;
import dk.jk.expenses.repository.TreeNodeRepository;
import dk.jk.expenses.service.TreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/posting")
@RequiredArgsConstructor
public class PostingController {

    private final PostingRepository postingRepository;
    private final TreeNodeRepository nodeRepository;
    private final TreeNodeRepository treeNodeRepository;

    @GetMapping
    public Collection<Posting> getPostings(@RequestParam(name = "treeNodeId", required = false) Long treeNodeId) {
        TreeNode treeNode = nodeRepository.getReferenceById(treeNodeId);
        return getChildren(treeNode)
                .flatMap(n -> n.getPostings().stream())
                .toList();
    }

    private Stream<TreeNode> getChildren(TreeNode node) {
        var children = node.getChildren();
        if (children.isEmpty()) {
            return Stream.of(node);
        }
        return children.stream().flatMap(n -> Stream.concat(Stream.of(node), getChildren(n)));
    }

}
