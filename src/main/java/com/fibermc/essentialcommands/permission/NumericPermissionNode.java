package com.fibermc.essentialcommands.permission;

public class NumericPermissionNode extends PermissionNode {

    private final int numericValue;

    public NumericPermissionNode(String node, int numericValue) {
        super(node + numericValue);
        this.numericValue = numericValue;
    }

    public int getNumericValue() {
        return numericValue;
    }
}
