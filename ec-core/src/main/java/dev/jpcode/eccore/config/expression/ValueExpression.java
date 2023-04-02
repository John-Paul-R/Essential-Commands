package dev.jpcode.eccore.config.expression;

public record ValueExpression<T>(T value) implements Expression {

    @Override
    public String serialize() {
        return this.value.toString();
    }
}
