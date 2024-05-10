package ast.expressions;

import ast.*;
import ast.types.*;
import instructions.*;

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
    public Source toStackInstructions(BasicBlock block, IrFunction func) {
        // evaluate left and right of the dot
        Source arrSource = left.toStackInstructions(block, func);
        Source indexSource = index.toStackInstructions(block, func);
        return evalIndex(block, arrSource, indexSource);
    }

    @Override
    public Source toSSAInstructions(BasicBlock block, IrFunction func) {
        // evaluate left and right of the dot
        Source arrSource = left.toSSAInstructions(block, func);
        Source indexSource = index.toSSAInstructions(block, func);
        return evalIndex(block, arrSource, indexSource);
    }

    private Source evalIndex(BasicBlock block, Source arrSource, Source indexSource) {
        // create a register that holds the pointer to the array item
        Register gepResult = Register.genMemberRegister(arrSource.getType().copy(), block.getLabel());

        // verify that the type of arrSource is pointer before casting
        if (!(arrSource.getType() instanceof ArrayType)) {
            throw new RuntimeException("Can't deref a Non-Pointer");
        }

        // create a register that holds the result of the load
        Register loadResult = Register.genTypedLocalRegister(new IntType(), block.getLabel());

        // create all instructions and add them to the basic block
        GetElemPtrInstruction gep = new GetElemPtrInstruction(gepResult, arrSource, indexSource);
        LoadInstruction load = new LoadInstruction(loadResult, gepResult);

        block.addCode(gep);
        block.addCode(load);

        // return the last register
        return loadResult;
    }
}
