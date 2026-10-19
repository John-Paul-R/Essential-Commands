package com.fibermc.essentialcommands.types;

import java.util.Arrays;
import java.util.Optional;

public enum AllowTeleportRequests {
    // Requests are delivered as normal.
    ON("on"),
    // Requests are auto-denied, but the receiver is notified that one was attempted.
    OFF("off"),
    // Requests are auto-denied, and nothing is sent to the receiver (spam prevention).
    OFF_SILENT("off-silent");

    private final String id;

    AllowTeleportRequests(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Optional<AllowTeleportRequests> fromId(String id) {
        return Arrays.stream(values())
            .filter(value -> value.id.equals(id))
            .findFirst();
    }
}
