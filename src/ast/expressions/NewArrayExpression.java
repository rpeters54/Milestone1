package ast.expressions;

import ast.*;
import ast.types.*;
import instructions.*;

public class NewArrayExpression
    extends AbstractExpression
{
    private final String size;

    public NewArrayExpression(int lineNum, String size)
    {
        super(lineNum);
        this.size = size;
    }

    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        try {
            int sizeVal = Integer.parseInt(size);
            if (sizeVal <= 0) {
                throw new TypeException(String.format("NewArrayExpression: Size Must Be" +
                        "Non-Negative, not %d, line %d", sizeVal, getLineNum()));
            }
        } catch (NumberFormatException e) {
            throw new TypeException(String.format("NewArrayExpression: Invalid Size" +
                    "Parameter Type %s, line %d", size, getLineNum()));
        }
        return new ArrayType();
    }

    @Override
    public Source toStackInstructions(BasicBlock block, IrFunction func) {
        return evalNewArray(block);
    }

    @Override
    public Source toSSAInstructions(BasicBlock block, IrFunction func) {
        return evalNewArray(block);
    }

    private Source evalNewArray(BasicBlock block) {
        // define next regs
        Register allocaResult = Register.genTypedLocalRegister(new PointerType(new ArrayAllocType(size)), block.getLabel());
        Register castResult = Register.genTypedLocalRegister(new ArrayType(), block.getLabel());

        //define instruction strings
        AllocaInstruction alloca = new AllocaInstruction(allocaResult);
        BitcastInstruction bitcast = new BitcastInstruction(castResult, allocaResult);

        //add instruction strings to basic block
        block.addCode(alloca);
        block.addCode(bitcast);

        //return value that represents array
        return castResult;
    }
}
