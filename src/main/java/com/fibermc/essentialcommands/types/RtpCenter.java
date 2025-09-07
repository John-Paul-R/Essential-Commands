package com.fibermc.essentialcommands.types;

import java.util.Optional;

public sealed interface RtpCenter permits RtpCenter.Spawn, RtpCenter.Coordinates {

    Optional<Vec2i> getPosition();

    String serialize();

    static RtpCenter.Spawn spawn() {
        return new Spawn();
    }

    static RtpCenter.Coordinates coordinates(int x, int z) {
        return new Coordinates(new Vec2i(x, z));
    }

    record Spawn() implements RtpCenter {
        @Override
        public Optional<Vec2i> getPosition() {
            return Optional.empty();
        }

        @Override
        public String serialize() {
            return "SPAWN";
        }
    }

    record Coordinates(Vec2i position) implements RtpCenter {
        @Override
        public Optional<Vec2i> getPosition() {
            return Optional.of(position);
        }

        @Override
        public String serialize() {
            return "COORDINATES:" + position.x() + "," + position.z();
        }
    }

    static RtpCenter parse(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot parse null or empty string");
        }

        String trimmed = serialized.trim();

        if ("SPAWN".equals(trimmed)) {
            return new Spawn();
        }

        if (trimmed.startsWith("COORDINATES:")) {
            String coords = trimmed.substring("COORDINATES:".length());
            String[] parts = coords.split(",");

            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid coordinates format: " + coords);
            }

            try {
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                return new Coordinates(new Vec2i(x, y));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid coordinate numbers: " + coords, e);
            }
        }

        throw new IllegalArgumentException("Unknown RtpCenter format: " + serialized);
    }
}
