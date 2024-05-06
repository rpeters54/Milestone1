package instructions;

public class BitcastInstruction implements Instruction {
    private Register result;
    private Source uncasted;

    public BitcastInstruction(Register result, Source uncasted) {
        this.result = result;
        this.uncasted = uncasted;
    }

    @Override
    public String toString() {
        return String.format("%s = bitcast %s %s to %s",
                result.getValue(), uncasted.getTypeString(),
                uncasted.getValue(), result.getTypeString());
    }

    @Override
    public void substitute(Source item, Source replacement) {
        if (item.equals(result)) {
            if (replacement instanceof Register) {
                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("BitcastInstruction: Tried to replace necessary Register with Source");
        }
        if (item.equals(uncasted)) {
            replacement.setLabel(uncasted.getLabel());
            uncasted = replacement;
        }
    }
}
