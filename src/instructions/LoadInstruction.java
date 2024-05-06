package instructions;

public class LoadInstruction implements Instruction {
    private Register lVal;
    private Source loc;

    public LoadInstruction(Register lVal, Source loc) {
        this.lVal = lVal;
        this.loc = loc;
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(loc.getTypeString());
        // <result> = load <ty>, <ty>* <source>
        return String.format("%s = load %s, %s %s",
                lVal.getValue(), deref, loc.getTypeString(), loc.getValue());
    }

    @Override
    public void substitute(Source item, Source replacement) {
        if (item.equals(lVal)) {
            if (replacement instanceof Register) {
                replacement.setLabel(lVal.getLabel());
                lVal = (Register) replacement;
            }
            throw new RuntimeException("LoadInstruction: Tried to replace necessary Register with Source");
        }
        if (item.equals(loc)) {
            replacement.setLabel(loc.getLabel());
            loc = replacement;
        }
    }
}
