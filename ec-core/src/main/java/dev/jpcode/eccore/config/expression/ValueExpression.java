package dev.jpcode.eccore.config.expression;

public record ValueExpression<T>(T value) implements ExpressionOperand {

    @Override
    public String serialize() {
        return this.value.toString();
    }
}
