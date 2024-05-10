package instructions;

public class ConditionalBranchInstruction implements JumpInstruction {
    private Source cond;
    private Label trueStub;
    private Label falseStub;

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
    public void substituteSource(Source original, Source replacement) {
        if (cond.equals(original)) {
//            replacement.setLabel(cond.getLabel());
            cond = replacement;
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (trueStub.equals(original))
            trueStub = replacement;
        if (falseStub.equals(original))
            falseStub = replacement;
    }

}
