package com.fibermc.essentialcommands.permission;

public class PermissionNode {

    final String node;

    public PermissionNode(String node) {
        this.node = node;
    }

    public String getString() {
        return node;
    }

//    public boolean equals(PermissionNode other) {
//        return this.getString().equals(other.getString());
//    }
}
