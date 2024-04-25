package ast.lvalues;

import ast.*;
import ast.types.PointerType;
import ast.types.Type;
import instructions.Register;
import instructions.Source;

public class LvalueId implements Lvalue {
    private final int lineNum;
    private final String id;

    public LvalueId(int lineNum, String id) {
        this.lineNum = lineNum;
        this.id = id;
    }

    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        return env.lookup(id);
    }

    @Override
    public Source genInst(BasicBlock block, LLVMEnvironment env) {
        return env.lookupReg(id);
    }
}
