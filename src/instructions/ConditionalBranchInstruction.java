package instructions;

public class ConditionalBranchInstruction implements JumpInstruction {
    private Source cond;
    private final Label trueStub;
    private final Label falseStub;

    public ConditionalBranchInstruction(Source cond, Label trueStub, Label falseStub) {
        this.cond = cond;
        this.trueStub = trueStub;
        this.falseStub = falseStub;
    }

    @Override
    public String toString() {
        return String.format("br %s %s, label %%%s, label %%%s",
                cond.getTypeString(), cond.getValue(), trueStub.getValue(), falseStub.getValue());
    }

    @Override
    public void substitute(Source item, Source replacement) {
        if (cond.equals(item)) {
            replacement.setLabel(cond.getLabel());
            cond = replacement;
        }
    }
}
