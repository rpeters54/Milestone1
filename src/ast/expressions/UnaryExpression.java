package ast.expressions;

import ast.*;
import ast.types.*;
import instructions.llvm.BinaryLLVMInstruction;
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
    public Source toStackInstructions(BasicBlock block, IrFunction func) {
        Source operandData = operand.toStackInstructions(block, func);
        return evalUnary(block, func, operandData);
    }

    @Override
    public Source toSSAInstructions(BasicBlock block, IrFunction func) {
        Source operandData = operand.toSSAInstructions(block, func);
        return evalUnary(block, func, operandData);
    }

    private Source evalUnary(BasicBlock block, IrFunction func, Source operandData) {
        Register unaryResult = Register.genLocalRegister(block.getLabel());

        switch (operator) {
            case MINUS -> {
                Literal zero = new Literal(new IntType(), "0", block.getLabel());
                unaryResult.setType(new IntType());
                BinaryLLVMInstruction binop = new BinaryLLVMInstruction(unaryResult,
                        BinaryExpression.Operator.MINUS, zero, operandData);
                block.addCode(binop);

                return unaryResult;
            }
            case NOT -> {
                Literal one = new Literal(new BoolType(), "true", block.getLabel());
                unaryResult.setType(new BoolType());
                BinaryLLVMInstruction binop = new BinaryLLVMInstruction(unaryResult,
                        BinaryExpression.Operator.XOR, one, operandData);
                block.addCode(binop);

                return unaryResult;
            }
            default -> throw new IllegalArgumentException("Invalid operand");
        }
    }
}
