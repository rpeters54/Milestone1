package instructions;

import ast.expressions.BinaryExpression;

public class BinaryInstruction implements Instruction {
    private LocalRegister lVal;
    private Source left;
    private Source right;
    private BinaryExpression.Operator op;

}
