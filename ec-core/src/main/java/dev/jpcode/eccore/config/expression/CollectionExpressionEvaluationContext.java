package dev.jpcode.eccore.config.expression;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class CollectionExpressionEvaluationContext<TOperand> implements ExpressionEvaluationContext<TOperand> {

    private final HashSet<TOperand> values;

    public CollectionExpressionEvaluationContext(Collection<TOperand> values) {
        this.values = new HashSet<>(values);
    }

    private CollectionExpressionEvaluationContext(TOperand[] values) {
        this.values = new HashSet<>();
        this.values.addAll(Arrays.asList(values));
    }

    @SafeVarargs
    public static <T> CollectionExpressionEvaluationContext<T> from(T ...values) {
        return new CollectionExpressionEvaluationContext<>(values);
    }

    @Override
    public boolean matches(TOperand value) {
        return this.values.contains(value);
    }

}
