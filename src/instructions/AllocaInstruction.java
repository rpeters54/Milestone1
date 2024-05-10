package instructions;

public class AllocaInstruction implements Instruction {
    private Register loc;

    public AllocaInstruction(Register loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        // the register holds a pointer to the type being allocated
        // I need to remove the '*' character that would be generated
        String ptr = loc.getTypeString();
        String deref = ptr.substring(0, ptr.length()-1);
        // <result> = alloca <ty>
        return String.format("%s = alloca %s", loc.getValue(), deref);
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(loc)) {
            if (replacement instanceof Register) {
//                replacement.setLabel(loc.getLabel());
                loc = (Register) replacement;
            }
            throw new RuntimeException("AllocaInstruction: Tried to replace necessary Register with Source");
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (loc.getLabel().equals(original))
            loc.setLabel(replacement);
    }
}
