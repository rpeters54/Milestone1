package instructions;

public class ReturnInstruction implements JumpInstruction {
    private Source retVal;

    public ReturnInstruction(Source retVal) {
        this.retVal = retVal;
    }

    @Override
    public String toString() {
        return String.format("ret %s %s", retVal.getTypeString(), retVal.getValue());
    }
}
