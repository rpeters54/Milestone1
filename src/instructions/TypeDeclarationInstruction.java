package instructions;

import ast.Declaration;
import ast.TypeDeclaration;
import ast.types.Type;

import java.util.List;
import java.util.stream.Collectors;

public class TypeDeclarationInstruction implements Instruction {
    private final String name;
    private final List<Type> memberTypes;

    public TypeDeclarationInstruction(TypeDeclaration td) {
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
}
