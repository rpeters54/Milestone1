package instructions;

public class LoadInstruction implements Instruction {
    private Register result;
    private Source loc;

    public LoadInstruction(Register result, Source loc) {
        this.result = result;
        this.loc = loc;
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(loc.getTypeString());
        // <result> = load <ty>, <ty>* <source>
        return String.format("%s = load %s, %s %s",
                result.getValue(), deref, loc.getTypeString(), loc.getValue());
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(result)) {
            if (replacement instanceof Register) {
//                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("LoadInstruction: Tried to replace necessary Register with Source");
        }
        if (original.equals(loc)) {
//            replacement.setLabel(loc.getLabel());
            loc = replacement;
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (result.getLabel().equals(original))
            result.setLabel(replacement);
        if (loc.getLabel().equals(original))
            loc.setLabel(replacement);
    }
}
