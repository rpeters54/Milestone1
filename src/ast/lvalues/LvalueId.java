package ast.lvalues;

import ast.*;
import ast.lvalues.Lvalue;
import ast.types.Type;

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
    public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
        return null;
    }
}
