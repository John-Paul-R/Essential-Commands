package dev.jpcode.eccore.config.expression;

import java.util.Arrays;
import java.util.function.Function;

public record BinaryExpression<TOperand>(
    Expression<TOperand> left,
    Expression<TOperand> right,
    LogicalOperator op
) implements Expression<TOperand> {

    public static <TOperand2> BinaryExpression<TOperand2>
    parse(String str, Function<String, TOperand2> operandParser) {
        var triplet = Arrays.stream(str.split(" "))
            .filter(el -> el.length() > 0)
            .toArray(String[]::new);
        var left = operandParser.apply(triplet[0]);

        var op = LogicalOperator.valueOf(triplet[1]);

        var right = operandParser.apply(triplet[2]);

        return new BinaryExpression<>(
            new ValueExpression<>(left),
            new ValueExpression<>(right),
            op);
    }

    @Override
    public String serialize() {
        return "(%s %s %s)".formatted(left.serialize(), op, right.serialize());
    }

    @Override
    public boolean matches(ExpressionEvaluationContext<TOperand> context) {
        return switch (op) {
            case OR -> left.matches(context) || right.matches(context);
            case AND -> left.matches(context) && right.matches(context);
        };
    }
}
