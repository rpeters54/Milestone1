package instructions;

public class StoreInstruction implements Instruction {
    private Register loc;
    private Source value;

    public StoreInstruction(Register loc, Source item) {
        this.loc = loc;
        this.value = item;
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(loc.getTypeString());
        return String.format("store %s %s, %s %s",
                deref, value.getValue(),
                loc.getTypeString(), loc.getValue());
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(loc)) {
            if (replacement instanceof Register) {
//                replacement.setLabel(loc.getLabel());
                loc = (Register) replacement;
            }
            throw new RuntimeException("StoreInstruction: Tried to replace necessary Register with Source");
        }
        if (original.equals(value)) {
//            replacement.setLabel(value.getLabel());
            value = replacement;
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (loc.getLabel().equals(original))
            loc.setLabel(replacement);
        if (value.getLabel().equals(original))
            value.setLabel(replacement);
    }
}
