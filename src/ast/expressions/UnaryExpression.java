package ast.expressions;

import ast.*;
import ast.types.BoolType;
import ast.types.IntType;
import ast.types.Type;

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

    public static enum Operator {
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
    public Value genInst(BasicBlock block, LLVMEnvironment env) {
        Value operandData = operand.genInst(block, env);
        String reg = env.getNextReg();
        switch (operator) {
            case MINUS -> {
                Value zero = new Value(env, new IntType(), "0");
                block.addCode(LLVMPrinter.binop(reg, "sub", zero, operandData));
                return new Value(env, new IntType(), reg);
            }
            case NOT -> {
                Value inv = new Value(env, new BoolType(), "1");
                block.addCode(LLVMPrinter.binop(reg, "xor", inv, operandData));
                return new Value(env, new BoolType(), reg);
            }
            default -> {throw new IllegalArgumentException("Invalid operand");}
        }
    }
}
