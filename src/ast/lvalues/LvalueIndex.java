package ast.lvalues;

import ast.*;
import ast.expressions.Expression;
import ast.types.*;
import instructions.llvm.GetElemPtrLLVMInstruction;
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


    public String getId() {
        throw new IllegalArgumentException("LValueIndex getId(): Arr has no Id");
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
    public Source toStackInstructions(BasicBlock block, IrFunction func) {
        Source arrData = left.toStackInstructions(block, func);
        Source indexData = index.toStackInstructions(block, func);
        return evalLvalIndex(block, func, arrData, indexData);
    }

    @Override
    public Source toSSAInstructions(BasicBlock block, IrFunction func) {
        Source arrData = left.toSSAInstructions(block, func);
        Source indexData = index.toSSAInstructions(block, func);
        return evalLvalIndex(block, func, arrData, indexData);
    }

    private Source evalLvalIndex(BasicBlock block, IrFunction func, Source arrData, Source indexData) {
        Register gepResult = Register.genTypedLocalRegister(arrData.getType().copy(), block.getLabel());

        GetElemPtrLLVMInstruction gep = new GetElemPtrLLVMInstruction(gepResult, arrData, indexData);
        block.addCode(gep);

        return gepResult;
    }
}
