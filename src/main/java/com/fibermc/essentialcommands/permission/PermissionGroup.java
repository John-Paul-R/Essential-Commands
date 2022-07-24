package com.fibermc.essentialcommands.permission;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class PermissionGroup<TNode extends PermissionNode> {
    protected final List<TNode> nodes;

    public PermissionGroup(Collection<TNode> nodes) {
        this.nodes = nodes.stream().toList();
    }

    public List<TNode> toList() {
        return nodes.stream().toList();
    }

    public Stream<String> streamStrings() {
        return nodes.stream().map(PermissionNode::getString);
    }

    public Stream<TNode> streamNodes() {
        return nodes.stream();
    }

    public static PermissionGroup<PermissionNode> of(PermissionNode... nodes) {
        return new PermissionGroup<>(Arrays.stream(nodes).toList());
    }

    public static PermissionGroup<PermissionNode> ofStrings(String... nodes) {
        return new PermissionGroup<>(Arrays.stream(nodes).map(PermissionNode::new).toList());
    }

    public int size() {
        return nodes.size();
    }
}
