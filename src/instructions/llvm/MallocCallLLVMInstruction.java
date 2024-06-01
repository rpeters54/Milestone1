package instructions.llvm;

import ast.declarations.TypeDeclaration;
import ast.types.IntType;
import instructions.Instruction;
import instructions.Literal;
import instructions.Register;
import instructions.Source;
import instructions.arm.BranchLinkArmInstruction;
import instructions.arm.MovArmInstruction;

import java.util.ArrayList;
import java.util.List;

public class MallocCallLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {

    private final TypeDeclaration td;

    public MallocCallLLVMInstruction(Register result, TypeDeclaration td) {
        super(result, new ArrayList<>());
        // generate literal referring to struct size
        Literal size = new Literal(new IntType(), Integer.toString(td.getLLVMSize()), result.getLabel());
        addSource(size);
        this.td = td;
    }

    private Source size() {
        return super.getSource(0);
    }

    @Override
    public String toString() {
        return String.format("%s = call i8* @malloc(%s %s)",
               getResult().llvmString(), size().getTypeString(), size().llvmString());
    }

    @Override
    public List<Instruction> toArm() {
        List<Instruction> instList = new ArrayList<>();
        instList.add(new MovArmInstruction(Register.genArmRegister(0), getSource(0)));
        instList.add(new BranchLinkArmInstruction("malloc", 1));
        instList.add(new MovArmInstruction(getResult(), Register.genArmRegister(0)));
        return instList;
    }
}
