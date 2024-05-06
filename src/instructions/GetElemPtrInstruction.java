package instructions;

public class GetElemPtrInstruction implements Instruction {
    private Register lVal;
    private Source obj;
    private Source index;

    public GetElemPtrInstruction(Register lVal, Source item, Source index) {
        this.lVal = lVal;
        this.obj = item;
        this.index = index;
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(obj.getTypeString());

        return String.format("%s = getelementptr inbounds %s, %s %s, i32 0, i32 %s",
                lVal.getValue(), deref, obj.getTypeString(), obj.getValue(), index.getValue());
    }

    public void substitute(Source item, Source replacement) {
        if (item.equals(lVal)) {
            if (replacement instanceof Register) {
                replacement.setLabel(lVal.getLabel());
                lVal = (Register) replacement;
            }
            throw new RuntimeException("GetElemPtrInstruction: Tried to replace necessary Register with Source");
        }
        if (item.equals(obj)) {
            replacement.setLabel(obj.getLabel());
            obj = replacement;
        }
        if (item.equals(index)) {
            replacement.setLabel(index.getLabel());
            index = replacement;
        }
    }
}
