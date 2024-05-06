package ast.lvalues;

import ast.*;
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

    public String getId() {
        return id;
    }

    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        return env.lookup(id);
    }

    @Override
    public Source toStackInstructions(BasicBlock block, IrFunction func) {
        return func.lookupReg(id);
    }

    @Override
    public Source toSSAInstructions(BasicBlock block, IrFunction func) {
        return null;
    }
}
