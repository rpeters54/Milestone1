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
    public Source genInst(BasicBlock block, LLVMEnvironment env) {
        // define next regs
        Register allocaResult = new Register(new PointerType(new ArrayAllocType(size)));
        Register castResult = new Register(new ArrayType());

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
