package instructions;

import ast.types.ArrayType;
import ast.types.StructType;

public class GetElemPtrInstruction implements Instruction {
    private Register result;
    private Source obj;
    private Source index;

    public GetElemPtrInstruction(Register result, Source item, Source index) {
        this.result = result;
        this.obj = item;
        this.index = index;
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(obj.getTypeString());
        if (obj.getType() instanceof StructType) {
            return String.format("%s = getelementptr inbounds %s, %s %s, i32 0, i32 %s",
                    result.getValue(), deref, obj.getTypeString(), obj.getValue(), index.getValue());
        }
        if (obj.getType() instanceof ArrayType) {
            return String.format("%s = getelementptr inbounds %s, %s %s, i64 %s",
                    result.getValue(), deref, obj.getTypeString(), obj.getValue(), index.getValue());
        }
        return "invalid";
    }

    public void substituteSource(Source original, Source replacement) {
        if (original.equals(result)) {
            if (replacement instanceof Register) {
//                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("GetElemPtrInstruction: Tried to replace necessary Register with Source");
        }
        if (original.equals(obj)) {
//            replacement.setLabel(obj.getLabel());
            obj = replacement;
        }
        if (original.equals(index)) {
//            replacement.setLabel(index.getLabel());
            index = replacement;
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (result.getLabel().equals(original))
            result.setLabel(replacement);
        if (obj.getLabel().equals(original))
            obj.setLabel(replacement);
        if (index.getLabel().equals(original))
            index.setLabel(replacement);
    }
}
