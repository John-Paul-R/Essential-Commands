package dev.jpcode.eccore.config.expression;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.Function;

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

    public static <T2> Expression parse(String str, Function<String, T2> operandParser) {
        try {
            return new PatternMatchingExpressionReader<>(str, operandParser).readExpression();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private enum Mode
    {
        Operand1,
        Operator1,
        Operand2,
        Operator2,
        Operand3,
    }

    private class ParsingContext
    {
        StringBuilder workingBuffer = new StringBuilder();
        public Mode mode = Mode.Operand1;
        public Expression operand1 = null;
        public LogicalOperator operator1 = null;
        public Expression operand2 = null;
        public LogicalOperator operator2 = null;
        public Expression operand3 = null;

        public void finalizeOperand3() {
            // do nothing if operator2 is null;
            if (this.operator2 == null) {
                return;
            }
            switch (this.operator2) {
                case AND -> {
                    this.operand2 = new BinaryExpression<T>(
                        this.operand2,
                        this.operand3,
                        this.operator2
                    );
                    this.operand3 = null;
                    this.operator2 = null;
                    this.mode = Mode.Operator2;
                }
                case OR -> {
                    this.operand1 = new BinaryExpression<T>(
                        this.operand1,
                        this.operand2,
                        this.operator1
                    );
                    // Shift everything else left 1
                    this.operator1 = this.operator2;
                    this.operator2 = null;
                    this.operand2 = this.operand3;
                    this.operand3 = null;
                    this.mode = Mode.Operator1;
                }
            }
        }

        public Expression fullFinalize() {
            finalizeOperand3();

            if (this.operand2 != null) {
                this.operand1 = new BinaryExpression<T>(
                    this.operand1,
                    this.operand2,
                    this.operator1
                );
            }

            return this.operand1;
        }

        private void tokenEnd() {
            switch (this.mode) {
                case Operand1 -> {
                    this.operand1 = readValueExpression(this.workingBuffer.toString());
                    this.mode = Mode.Operator1;
                }
                case Operator1 -> {
                    this.operator1 = LogicalOperator.valueOf(this.workingBuffer.toString());
                    this.mode = Mode.Operand2;
                }
                case Operand2 -> {
                    this.operand2 = readValueExpression(this.workingBuffer.toString());
                    this.mode = Mode.Operator2;
                }
                case Operator2 -> {
                    this.operator2 = LogicalOperator.valueOf(this.workingBuffer.toString());
                    this.mode = Mode.Operand3;
                }
                case Operand3 -> {
                    this.operand3 = readValueExpression(this.workingBuffer.toString());
                    finalizeOperand3();
                }
            }
            this.workingBuffer = new StringBuilder();
        }

    }

    public Expression readExpression() throws IOException {
        return parse(this.reader);
    }

    private Expression parse(StringReader reader) throws IOException {
        final ParsingContext ctx = new ParsingContext();

        int chInt = -2;
        while (chInt != -1) {
            chInt = reader.read();
            char ch = (char)chInt;

            switch (ch) {
                case ' ' -> {
                    if (ctx.workingBuffer.isEmpty()) {
                        continue;
                    }
                    ctx.tokenEnd();
                }
                case '(' -> { // begin group
                    var parsedGroup = parse(reader);
                    switch (ctx.mode) {
                        case Operand1 -> {ctx.operand1 = parsedGroup; ctx.mode = Mode.Operator1;}
                        case Operand2 -> {ctx.operand2 = parsedGroup; ctx.mode = Mode.Operator2;}
                        case Operand3 -> {ctx.operand3 = parsedGroup; ctx.finalizeOperand3();}
                        default -> throw new RuntimeException("Invalid parsing state (Group start while not ready to parse operand (missing logical operator?)");
                    }
                }
                case ')' -> { // end group
                    ctx.tokenEnd();
                    return ctx.fullFinalize();
                }
                default -> ctx.workingBuffer.append(ch);
            }
        }

        return ctx.fullFinalize();
    }

    private ValueExpression<T> readValueExpression(String str) {
        return new ValueExpression<>(this.operandParser.apply(str));
    }
}
