package ast.expressions;

import ast.*;
import ast.types.ArrayType;
import ast.types.IntType;
import ast.types.PointerType;
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
    public Value genInst(BasicBlock block, LLVMEnvironment env) {
        // evaluate left and right of the dot
        Value arrData = left.genInst(block, env);
        Value indexData = index.genInst(block, env);
        // get the next register and generate a getelementptr inst.
        String reg = env.getNextReg();
        block.addCode(LLVMPrinter.GEP(reg, arrData, indexData));
        // with the pointer to the array location found
        // generate a load instruction that loads the value at the location
        Type arrType = arrData.getType();
        PointerType p = new PointerType(arrType);
        Value arrPointer = new Value(env, p, reg);
        reg = env.getNextReg();
        block.addCode(LLVMPrinter.load(reg, arrPointer));
        // return a value that defines what was loaded
        return new Value(env, arrType, reg);
    }
}
