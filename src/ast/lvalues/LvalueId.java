package ast.lvalues;

import ast.*;
import ast.types.PointerType;
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
    public Value genInst(BasicBlock block, LLVMEnvironment env) {
        Type type = env.lookupTypeBinding(id);
        return new Value(env, new PointerType(type), env.lookupRegBinding(id));
    }
}
