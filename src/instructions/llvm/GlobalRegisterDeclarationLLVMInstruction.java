package instructions.llvm;

import ast.types.PointerType;
import ast.types.StructType;
import instructions.Instruction;
import instructions.Register;
import instructions.arm.ArmInstruction;
import instructions.arm.GlobalDeclarationArmInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GlobalRegisterDeclarationLLVMInstruction extends AbstractLLVMInstruction {

    public GlobalRegisterDeclarationLLVMInstruction(Register result) {
        super(result, new ArrayList<>());
    }

    @Override
    public String toString() {
        // the register holds a pointer to the type being allocated
        // I need to remove the '*' character that would be generated
        String deref = TypeMap.deref(getResult().getTypeString());
        PointerType type = (PointerType) getResult().getType();
        String baseValue;
        if (type.getBaseType() instanceof PointerType || type.getBaseType() instanceof StructType) {
            baseValue = "null";
        } else {
            baseValue = "0";
        }
        return String.format("%s = common global %s %s", getResult().llvmString(), deref, baseValue);
    }

    @Override
    public List<Instruction> toArm() {
        return Collections.singletonList(new GlobalDeclarationArmInstruction(getResult().toString()));
    }
}
