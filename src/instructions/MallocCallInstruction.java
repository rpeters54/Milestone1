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
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(result)) {
            if (replacement instanceof Register) {
//                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("MallocCallInstruction: Tried to replace necessary Register with Source");
        }
        if (original.equals(size)) {
//            replacement.setLabel(size.getLabel());
            size = replacement;
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (result.getLabel().equals(original))
            result.setLabel(replacement);
        if (size.getLabel().equals(original))
            size.setLabel(replacement);
    }
}
