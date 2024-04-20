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
    public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
        LLVMMetadata operandData = operand.genLLVM(block, env);
        int reg = env.getCurrentRegister();
        switch (operator) {
            case MINUS -> {
                Type zeroType = new IntType();
                LLVMMetadata zero = new LLVMMetadata(
                        zeroType, env.typeToString(zeroType), "0");
                block.addCode(LLVMPrinter.sprintBinop(reg, "sub", zero, operandData));
                return new LLVMMetadata(zeroType, env.typeToString(zeroType), reg);
            }
            case NOT -> {
                Type invType = new BoolType();
                LLVMMetadata inv = new LLVMMetadata(
                        invType, env.typeToString(invType), "1");
                block.addCode(LLVMPrinter.sprintBinop(reg, "xor", inv, operandData));
                return new LLVMMetadata(invType, env.typeToString(invType), reg);
            }
            default -> {return null;}
        }
    }
}
