package instructions;

import ast.types.PointerType;
import ast.types.StructType;

public class GlobalRegisterDeclarationInstruction implements Instruction {
    private Register self;

    public GlobalRegisterDeclarationInstruction(Register self) {
        this.self = self;
    }

    @Override
    public String toString() {
        // the register holds a pointer to the type being allocated
        // I need to remove the '*' character that would be generated
        String deref = TypeMap.deref(self.getTypeString());
        PointerType type = (PointerType) self.getType();
        String baseValue;
        if (type.getBaseType() instanceof PointerType || type.getBaseType() instanceof StructType) {
            baseValue = "null";
        } else {
            baseValue = "0";
        }
        return String.format("%s = common global %s %s", self.getValue(), deref, baseValue);
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(self)) {
            if (replacement instanceof Register) {
//                replacement.setLabel(self.getLabel());
                self = (Register) replacement;
            }
            throw new RuntimeException("GlobalRegisterDeclarationInstruction: Tried to replace necessary Register with Source");
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (self.getLabel().equals(original))
            self.setLabel(replacement);
    }
}
