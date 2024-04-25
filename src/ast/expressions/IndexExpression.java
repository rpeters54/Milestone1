package ast.expressions;

import ast.*;
import ast.types.ArrayType;
import ast.types.IntType;
import ast.types.PointerType;
import ast.types.Type;
import instructions.GetElemPtrInstruction;
import instructions.LoadInstruction;
import instructions.Register;
import instructions.Source;

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
    public Source genInst(BasicBlock block, LLVMEnvironment env) {
        // evaluate left and right of the dot
        Source arrSource = left.genInst(block, env);
        Source indexSource = index.genInst(block, env);

        // create a register that holds the pointer to the array item
        Register gepResult = new Register(arrSource.getType().copy());

        // verify that the type of arrSource is pointer before casting
        if (!(arrSource.getType() instanceof PointerType)) {
            throw new IllegalArgumentException("Can't deref a Non-Pointer");
        }
        Type baseType = ((PointerType) arrSource.getType()).getBaseType();

        // create a register that holds the result of the load
        Register loadResult = new Register(baseType.copy());

        // create both instructions and add them to the basic block
        GetElemPtrInstruction gep = new GetElemPtrInstruction(gepResult, arrSource, indexSource);
        LoadInstruction load = new LoadInstruction(loadResult, gepResult);

        block.addCode(gep);
        block.addCode(load);

        // return the last register
        return loadResult;
    }
}
