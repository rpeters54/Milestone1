package instructions;

import ast.types.Type;

public class ReturnInstruction implements JumpInstruction {
    private Type retType;
    private Source retVal;

    public ReturnInstruction(Type retType, Source retVal) {
        this.retType = retType;
        this.retVal = retVal;
    }

    @Override
    public String toString() {
        return String.format("ret %s %s", TypeMap.ttos(retType), retVal.getValue());
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(retVal)) {
//            replacement.setLabel(retVal.getLabel());
            retVal = replacement;
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (retVal.getLabel().equals(original))
            retVal.setLabel(replacement);
    }
}
