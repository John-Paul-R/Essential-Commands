package com.fibermc.essentialcommands.permission;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NumericPermissionGroup extends PermissionGroup<NumericPermissionNode> {
    private NumericPermissionGroup(Collection<NumericPermissionNode> nodes) {
        super(nodes);
    }

    public static NumericPermissionGroup create(String basePermission, Collection<Integer> numericValues) {
        String trueBasePermission = basePermission.endsWith(".") ? basePermission : basePermission + ".";
        var nodes = numericValues.stream()
            .sorted(Comparator.comparingInt(a -> -a)) // Order Descending
            .map(num -> new NumericPermissionNode(trueBasePermission, num))
            .toList();

        return new NumericPermissionGroup(nodes);
    }

    public NumericPermissionNode getHighest() {
        return super.nodes.get(0);
    }

    public NumericPermissionNode getLowest() {
        return super.nodes.get(super.nodes.size() - 1);
    }

    public Optional<NumericPermissionNode> getHighestGranted(Predicate<PermissionNode> checkGrantedFn) {
        return super.nodes.stream()
            .filter(checkGrantedFn)
            .findFirst();
    }

    public Optional<NumericPermissionNode> getLowestGranted(Predicate<PermissionNode> checkGrantedFn) {
        var iterator = super.nodes.listIterator();
        return Stream.generate(iterator::previous).limit(super.nodes.size())
            .filter(checkGrantedFn)
            .findFirst();
    }
}
