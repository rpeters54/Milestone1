package instructions;

public class MallocCallInstruction implements Instruction {
    private Register result;
    private Source size;

    public MallocCallInstruction(Register result, Source size) {
        this.result = result;
        this.size = size;
    }

    @Override
    public String toString() {
        return String.format("%s = call i8* @malloc(%s %s)",
                result.getValue(), size.getTypeString(), size.getValue());
    }

    @Override
    public void substitute(Source item, Source replacement) {
        if (item.equals(result)) {
            if (replacement instanceof Register) {
                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("MallocCallInstruction: Tried to replace necessary Register with Source");
        }
        if (item.equals(size)) {
            replacement.setLabel(size.getLabel());
            size = replacement;
        }
    }
}
