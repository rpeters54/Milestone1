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
}
