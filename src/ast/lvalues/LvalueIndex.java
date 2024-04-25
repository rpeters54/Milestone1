package ast.lvalues;

import ast.*;
import ast.expressions.Expression;
import ast.types.ArrayType;
import ast.types.IntType;
import ast.types.PointerType;
import ast.types.Type;
import instructions.GetElemPtrInstruction;
import instructions.Register;
import instructions.Source;

public class LvalueIndex implements Lvalue {
    private final int lineNum;
    private final Expression left;
    private final Expression index;

    public LvalueIndex(int lineNum, Expression left, Expression index) {
        this.lineNum = lineNum;
        this.left = left;
        this.index = index;
    }

    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        Type lType = left.typecheck(env);
        Type rType = index.typecheck(env);
        if (!(lType instanceof ArrayType)) {
            throw new TypeException(String.format("LValueIndex: Tried to " +
                    "Index into Non-Array, line %d", lineNum));
        }
        if (!(rType instanceof IntType)) {
            throw new TypeException(String.format("LValueIndex: Can't Have " +
                    "Non-Int Index, line %d", lineNum));
        }
        return new IntType();
    }

    @Override
    public Source genInst(BasicBlock block, LLVMEnvironment env) {
        Source arrData = left.genInst(block, env);
        Source indexData = index.genInst(block, env);
        Register gepResult = new Register(arrData.getType().copy());

        GetElemPtrInstruction gep = new GetElemPtrInstruction(gepResult, arrData, indexData);
        block.addCode(gep);

        return gepResult;
    }
}
