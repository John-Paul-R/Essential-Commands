package dev.jpcode.eccore.config.expression;

import dev.jpcode.eccore.config.StringSerializable;

public interface Expression<TOperand> extends StringSerializable {
    boolean matches(ExpressionEvaluationContext<TOperand> context);
}
