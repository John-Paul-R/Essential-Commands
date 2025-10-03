package com.fibermc.joinpoints.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinpointLimit {

    public enum JoinpointType {
        ANY, GLOBAL, SHARED, PRIVATE
    }

    private final Map<JoinpointType, List<Integer>> limits;

    private JoinpointLimit(Map<JoinpointType, List<Integer>> limits) {
        this.limits = new HashMap<>();
        for (var type : JoinpointType.values()) {
            var inputVal = limits.get(type);
            this.limits.put(type, Objects.requireNonNullElseGet(inputVal, List::of));
        }
    }

    public static JoinpointLimit any(Integer... limits) {
        return new JoinpointLimit(Map.of(JoinpointType.ANY, List.of(limits)));
    }

    public static JoinpointLimit global(Integer... limits) {
        return new JoinpointLimit(Map.of(JoinpointType.GLOBAL, List.of(limits)));
    }

    public static JoinpointLimit shared(Integer... limits) {
        return new JoinpointLimit(Map.of(JoinpointType.SHARED, List.of(limits)));
    }

    public static JoinpointLimit privateOnly(Integer... limits) {
        return new JoinpointLimit(Map.of(JoinpointType.PRIVATE, List.of(limits)));
    }

    public static JoinpointLimit combined(Map<JoinpointType, List<Integer>> limits) {
        return new JoinpointLimit(limits);
    }

    public boolean hasTypeLimit(JoinpointType type) {
        return limits.containsKey(type);
    }

    public Map<JoinpointType, List<Integer>> getLimits() {
        return new HashMap<>(limits);
    }

    /**
     * Parse joinpoint limit from string format
     * Examples:
     * - "any(1,2,3)" - Any type of joinpoint with limits 1,2,3
     * - "global(2,4,5)" - Only global joinpoints with limits 2,4,5
     * - "global(2,3,4),shared(1,3,4)" - Global and shared with different limits
     * - "private(5),global(2),shared(3)" - All three types with single limits
     */
    public static JoinpointLimit parse(String input) {
        Map<JoinpointType, List<Integer>> parsedLimits = new HashMap<>();

        // Pattern to match: type_name(number,number,number)
        Pattern pattern = Pattern.compile("(\\w+)\\(([0-9,\\s]+)\\)");
        Matcher matcher = pattern.matcher(input.toLowerCase().trim());

        while (matcher.find()) {
            String typeName = matcher.group(1).trim();
            String numbersStr = matcher.group(2).trim();

            // Parse the type
            JoinpointType type;
            try {
                type = JoinpointType.valueOf(typeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid joinpoint type: " + typeName + ". Valid types are: any, global, shared, private");
            }

            // Parse the numbers
            String[] numberStrs = numbersStr.split(",");
            List<Integer> numbers = new java.util.ArrayList<>();

            for (String numberStr : numberStrs) {
                try {
                    int number = Integer.parseInt(numberStr.trim());
                    if (number < 0) {
                        throw new IllegalArgumentException("Joinpoint limits cannot be negative: " + number);
                    }
                    numbers.add(number);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number in joinpoint limit: " + numberStr.trim());
                }
            }

            if (numbers.isEmpty()) {
                throw new IllegalArgumentException("Joinpoint type " + typeName + " must have at least one limit specified");
            }

            parsedLimits.put(type, numbers);
        }

        if (parsedLimits.isEmpty()) {
            throw new IllegalArgumentException("Invalid joinpoint limit format. Examples: 'any(1,2,3)', 'global(2,4,5)', 'global(2,3,4),shared(1,3,4)'");
        }

        return new JoinpointLimit(parsedLimits);
    }

    /**
     * Serialize joinpoint limit to string format
     */
    public static String serialize(JoinpointLimit limit) {
        if (limit == null || limit.limits.isEmpty()) {
            return "any(0)";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Map.Entry<JoinpointType, List<Integer>> entry : limit.limits.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;

            sb.append(entry.getKey().name().toLowerCase());
            sb.append("(");

            List<Integer> numbers = entry.getValue();
            for (int i = 0; i < numbers.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(numbers.get(i));
            }

            sb.append(")");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return serialize(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        JoinpointLimit that = (JoinpointLimit) obj;
        return limits.equals(that.limits);
    }

    @Override
    public int hashCode() {
        return limits.hashCode();
    }
}
