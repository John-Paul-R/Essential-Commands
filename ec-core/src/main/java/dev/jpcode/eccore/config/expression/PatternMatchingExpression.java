package dev.jpcode.eccore.config.expression;

import java.util.Arrays;
import java.util.function.Function;

public record PatternMatchingExpression<TOperand>(
    ExpressionOperand left,
    ExpressionOperand right,
    LogicalOperator op
) implements ExpressionOperand {

    public static <TOperand2> PatternMatchingExpression<TOperand2>
    parse(String str, Function<String, TOperand2> operandParser) {
        var triplet = Arrays.stream(str.split(" "))
            .filter(el -> el.length() > 0)
            .toArray(String[]::new);
        var left = operandParser.apply(triplet[0]);

        var op = LogicalOperator.valueOf(triplet[1]);

        var right = operandParser.apply(triplet[2]);

        return new PatternMatchingExpression<>(
            new ValueExpression<>(left),
            new ValueExpression<>(right),
            op);
    }

    @Override
    public String serialize() {
        return "(%s %s %s)".formatted(left.serialize(), op, right.serialize());
    }

}
