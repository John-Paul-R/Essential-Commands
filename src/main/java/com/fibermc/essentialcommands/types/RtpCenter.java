package com.fibermc.essentialcommands.types;

import java.util.Optional;
import java.util.regex.Pattern;

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
            return "Spawn";
        }
    }

    record Coordinates(Vec2i position) implements RtpCenter {
        @Override
        public Optional<Vec2i> getPosition() {
            return Optional.of(position);
        }

        @Override
        public String serialize() {
            return "Coordinates(" + position.x() + "," + position.z() + ")";
        }

        private static final Pattern REGEX = Pattern.compile(
            "(?:COORDINATES)?\\(?\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)?",
            Pattern.CASE_INSENSITIVE
        );
    }

    static RtpCenter parse(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot parse null or empty string");
        }

        String trimmed = serialized.trim();

        if ("SPAWN".equalsIgnoreCase(trimmed)) {
            return new Spawn();
        }

        var matcher = Coordinates.REGEX.matcher(trimmed);

        if (matcher.matches()) {
            var xStr = matcher.group(1);
            var zStr = matcher.group(2);

            try {
                int x = Integer.parseInt(xStr);
                int y = Integer.parseInt(zStr);
                return new Coordinates(new Vec2i(x, y));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid coordinate numbers: ", e);
            }
        }

        throw new IllegalArgumentException("Unknown rtp_center format: " + serialized);
    }
}
