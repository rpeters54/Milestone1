package ast.expressions;

import ast.*;
import ast.types.ArrayType;
import ast.types.IntType;
import ast.types.Type;

public class IndexExpression
    extends AbstractExpression
{
    private final Expression left;
    private final Expression index;

    public IndexExpression(int lineNum, Expression left, Expression index)
    {
        super(lineNum);
        this.left = left;
        this.index = index;
    }

    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        Type lType = left.typecheck(env);
        Type rType = index.typecheck(env);
        if (!(lType instanceof ArrayType)) {
            throw new TypeException(String.format("IndexExpression: Tried to" +
                    "Index into Non-Array, line %d", getLineNum()));
        }
        if (!(rType instanceof IntType)) {
            throw new TypeException(String.format("IndexExpression: Can't Have" +
                    "Non-Int Index, line %d", getLineNum()));
        }
        return new IntType();
    }

    @Override
    public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
        return null;
    }
}
