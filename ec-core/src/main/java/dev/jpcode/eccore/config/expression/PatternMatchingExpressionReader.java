package dev.jpcode.eccore.config.expression;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.jpcode.eccore.function.Action;

public class PatternMatchingExpressionReader<T>
{
    private final StringReader reader;
    private final Function<String, T> operandParser;

    public PatternMatchingExpressionReader(String str, Function<String, T> operandParser) {
        this.reader = new StringReader(str);
        this.operandParser = operandParser;
    }

    public PatternMatchingExpressionReader(StringReader reader, Function<String, T> operandParser) {
        this.reader = reader;
        this.operandParser = operandParser;
    }

    public static <T2> ExpressionOperand parse(String str, Function<String, T2> operandParser) {
        try {
            return new PatternMatchingExpressionReader<>(str, operandParser).readExpression();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    enum Mode
    {
        Operand1,
        Operator1,
        Operand2,
        Operator2,
        Operand3,
    }

    static class ParsingContext
    {
        StringBuilder workingBuffer = new StringBuilder();
        public Mode mode = Mode.Operand1;
        public ExpressionOperand operand1 = null;
        public LogicalOperator operator1 = null;
        public ExpressionOperand operand2 = null;
        public LogicalOperator operator2 = null;
        public ExpressionOperand operand3 = null;
    }

    public ExpressionOperand readExpression() throws IOException {
        return parse(this.reader);
    }

    private ExpressionOperand parse(StringReader reader) throws IOException {
        int chInt = -2;
        final ParsingContext ctx = new ParsingContext();

        Action finalizeOperand3 = () -> {
            if (ctx.operator2 == LogicalOperator.AND) {
                ctx.operand2 = new PatternMatchingExpression<T>(
                    ctx.operand2,
                    ctx.operand3,
                    ctx.operator2
                );
                ctx.operand3 = null;
                ctx.operator2 = null;
                ctx.mode = Mode.Operator2;
            } else { // operator2 is LogicalOperator.OR
                ctx.operand1 = new PatternMatchingExpression<T>(
                    ctx.operand1,
                    ctx.operand2,
                    ctx.operator1
                );
                // Shift everything else left 1
                ctx.operator1 = ctx.operator2;
                ctx.operator2 = null;
                ctx.operand2 = ctx.operand3;
                ctx.operand3 = null;
                ctx.mode = Mode.Operator1;
            }
        };

        Supplier<ExpressionOperand> fullFinalize = () -> {
            finalizeOperand3.execute();

            if (ctx.operand2 != null) {
                ctx.operand1 = new PatternMatchingExpression<T>(
                    ctx.operand1,
                    ctx.operand2,
                    ctx.operator1
                );
            }

            return ctx.operand1;
        };

        Action tokenEnd = () -> {
            switch (ctx.mode) {
                case Operand1 -> {
                    ctx.operand1 = readValueExpression(ctx.workingBuffer.toString());
                    ctx.mode = Mode.Operator1;
                }
                case Operator1 -> {
                    ctx.operator1 = LogicalOperator.valueOf(ctx.workingBuffer.toString());
                    ctx.mode = Mode.Operand2;
                }
                case Operand2 -> {
                    ctx.operand2 = readValueExpression(ctx.workingBuffer.toString());
                    ctx.mode = Mode.Operator2;
                }
                case Operator2 -> {
                    ctx.operator2 = LogicalOperator.valueOf(ctx.workingBuffer.toString());
                    ctx.mode = Mode.Operand3;
                }
                case Operand3 -> {
                    ctx.operand3 = readValueExpression(ctx.workingBuffer.toString());
                    finalizeOperand3.execute();
                }
            }
            ctx.workingBuffer = new StringBuilder();
        };

        while (chInt != -1) {
            chInt = reader.read();
            char ch = (char)chInt;

            switch (ch) {
                case ' ' -> {
                    if (ctx.workingBuffer.isEmpty()) {
                        continue;
                    }
                    tokenEnd.execute();
                }
                case '(' -> { // begin group
                    var parsedGroup = parse(reader);
                    switch (ctx.mode) {
                        case Operand1 -> {ctx.operand1 = parsedGroup; ctx.mode = Mode.Operator1;}
                        case Operand2 -> {ctx.operand2 = parsedGroup; ctx.mode = Mode.Operator2;}
                        case Operand3 -> {ctx.operand3 = parsedGroup; finalizeOperand3.execute();}
                        default -> throw new RuntimeException("Invalid parsing state (Group start while not ready to parse operand (missing logical operator?)");
                    }
                }
                case ')' -> { // end group
                    tokenEnd.execute();
                    return fullFinalize.get();
                }
                default -> ctx.workingBuffer.append(ch);
            }
        }

        return fullFinalize.get();
    }


    private ValueExpression<T> readValueExpression(String str) {
        return new ValueExpression<>(this.operandParser.apply(str));
    }
}
