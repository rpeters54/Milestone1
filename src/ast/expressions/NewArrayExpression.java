package ast.expressions;

import ast.*;
import ast.expressions.AbstractExpression;
import ast.types.ArrayType;
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
            Integer.parseInt(size);
        } catch (NumberFormatException e) {
            throw new TypeException(String.format("NewArrayExpression: Invalid Size" +
                    "Parameter %s, line %d", size, getLineNum()));
        }
        return new ArrayType();
    }

    @Override
    public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
        return null;
    }
}
