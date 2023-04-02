package dev.jpcode.eccore.config.expression;

public record ValueExpression<T>(T value) implements Expression<T> {

    @Override
    public String serialize() {
        return this.value.toString();
    }

    @Override
    public boolean matches(ExpressionEvaluationContext<T> context) {
        return context.matches(value);
    }
}
