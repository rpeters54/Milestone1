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

    @Override
    public void substitute(Source item, Source replacement) {
        if (item.equals(retVal)) {
            replacement.setLabel(retVal.getLabel());
            retVal = replacement;
        }
    }
}
