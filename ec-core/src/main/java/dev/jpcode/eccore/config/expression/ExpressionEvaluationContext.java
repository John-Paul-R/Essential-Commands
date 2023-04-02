package dev.jpcode.eccore.config.expression;

public interface ExpressionEvaluationContext<TOperand> {
    boolean matches(TOperand operand);
}
