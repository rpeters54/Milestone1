package ast.expressions;

import ast.*;
import ast.types.BoolType;
import ast.types.IntType;
import ast.types.Type;
import instructions.BinaryInstruction;
import instructions.Literal;
import instructions.Register;
import instructions.Source;

public class UnaryExpression
        extends AbstractExpression {
    private final Operator operator;
    private final Expression operand;

    private UnaryExpression(int lineNum, Operator operator, Expression operand) {
        super(lineNum);
        this.operator = operator;
        this.operand = operand;
    }

    public static UnaryExpression create(int lineNum, String opStr,
                                         Expression operand) {
        if (opStr.equals(NOT_OPERATOR)) {
            return new UnaryExpression(lineNum, Operator.NOT, operand);
        } else if (opStr.equals(MINUS_OPERATOR)) {
            return new UnaryExpression(lineNum, Operator.MINUS, operand);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static final String NOT_OPERATOR = "!";
    private static final String MINUS_OPERATOR = "-";

    public enum Operator {
        NOT, MINUS
    }

    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        Type opType = operand.typecheck(env);
        switch (operator) {
            case MINUS -> {
                if (opType instanceof IntType) {
                    return opType;
                }
                throw new TypeException(String.format("UnaryExpression: Operand " +
                        "Wrong Type for Arithmetic Expression, line: %d", getLineNum()));
            }
            case NOT -> {
                if (opType instanceof BoolType) {
                    return opType;
                }
                throw new TypeException(String.format("UnaryExpression: Operands " +
                        "Wrong Type for Boolean Expression, line: %d", getLineNum()));
            }
            default -> throw new TypeException(String.format("UnaryExpression: " +
                    "Something Went Horribly Wrong, line: %d", getLineNum()));
        }
    }

    @Override
    public Source genInst(BasicBlock block, LLVMEnvironment env) {
        Source operandData = operand.genInst(block, env);
        Register unaryResult = new Register();

        switch (operator) {
            case MINUS -> {
                Literal zero = new Literal(new IntType(), "0");
                unaryResult.setType(new IntType());
                BinaryInstruction binop = new BinaryInstruction(unaryResult,
                        BinaryExpression.Operator.MINUS, zero, operandData);
                block.addCode(binop);
                return unaryResult;
            }
            case NOT -> {
                Literal one = new Literal(new BoolType(), "1");
                unaryResult.setType(new BoolType());
                BinaryInstruction binop = new BinaryInstruction(unaryResult,
                        BinaryExpression.Operator.XOR, one, operandData);
                block.addCode(binop);
                return unaryResult;
            }
            default -> throw new IllegalArgumentException("Invalid operand");
        }
    }
}
