package instructions.llvm;

import ast.declarations.Declaration;
import ast.declarations.TypeDeclaration;
import ast.types.Type;
import instructions.Label;
import instructions.Source;
import instructions.arm.ArmInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeDeclarationLLVMInstruction extends AbstractLLVMInstruction {
    private final String name;
    private final List<Type> memberTypes;

    public TypeDeclarationLLVMInstruction(TypeDeclaration td) {
        super(null, new ArrayList<>());
        name = td.getName();
        memberTypes = td.getFields().stream().map(Declaration::getType).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder typeList = new StringBuilder();
        for (Type memberType : memberTypes) {
            typeList.append(TypeMap.ttos(memberType)).append(", ");
        }
        if (typeList.length()>0) {
            typeList.delete(typeList.length()-2, typeList.length());
        }
        return String.format("%%struct.%s = type {%s}", name, typeList);
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        //do nothing
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        //do nothing
    }

    @Override
    public List<ArmInstruction> toArm() {
        throw new RuntimeException("Fix this first");
    }
}
