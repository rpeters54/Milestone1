package ast.expressions;

import ast.*;
import ast.types.ArrayType;
import ast.types.IntType;
import ast.types.PointerType;
import ast.types.Type;

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
    public Value genInst(BasicBlock block, LLVMEnvironment env) {
        // define next regs
        String reg = env.getNextReg();
        String reg2 = env.getNextReg();

        //define instruction strings
        String alloca = String.format("%s = alloca [%s x i64]", reg, size);
        String bitcast = String.format("%s = bitcast [%s x i64]* %s to i64*", reg2, size, reg);

        //add instruction strings to basic block
        block.addCode(alloca);
        block.addCode(bitcast);

        //return value that represents array
        return new Value(env, new PointerType(new IntType()), reg2);
    }
}
