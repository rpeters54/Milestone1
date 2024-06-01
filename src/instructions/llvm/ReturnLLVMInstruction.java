package instructions.llvm;

import ast.types.Type;
import instructions.Instruction;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.MovArmInstruction;
import instructions.arm.ReturnArmInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReturnLLVMInstruction extends AbstractLLVMInstruction implements JumpInstruction, CriticalInstruction {
    private final Type retType;

    public ReturnLLVMInstruction(Type retType, Source retVal) {
        super(null, new ArrayList<>());
        addSource(retVal);
        this.retType = retType;
    }

    @Override
    public String toString() {
        return String.format("ret %s %s", TypeMap.ttos(retType), getSource(0).llvmString());
    }

    @Override
    public List<Instruction> toArm() {
        return Arrays.asList(
                new MovArmInstruction(Register.genArmRegister(0), getSource(0)),
                new ReturnArmInstruction()
        );
    }

}
