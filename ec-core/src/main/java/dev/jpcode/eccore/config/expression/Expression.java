package dev.jpcode.eccore.config.expression;

import dev.jpcode.eccore.config.StringSerializable;

public interface Expression<TOperand> extends StringSerializable {
    boolean matches(ExpressionEvaluationContext<TOperand> context);

    static <T> Expression<T> of(T value) {
        return new Expression<>() {
            @Override
            public boolean matches(ExpressionEvaluationContext<T> context) {
                return context.matches(value);
            }

            @Override
            public String serialize() {
                return value.toString();
            }
        };
    }

    static <T> Expression<T> empty() {
        return new Expression<>() {
            @Override
            public boolean matches(ExpressionEvaluationContext<T> context) {
                return false;
            }

            @Override
            public String serialize() {
                return "";
            }
        };
    }
}
