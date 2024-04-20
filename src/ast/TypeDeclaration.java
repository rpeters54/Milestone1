package ast;

import ast.types.StructType;

import java.util.List;
import java.util.Objects;

public class TypeDeclaration {
    private final int lineNum;
    private final String name;
    private final List<Declaration> fields;

    public TypeDeclaration(int lineNum, String name, List<Declaration> fields) {
        this.lineNum = lineNum;
        this.name = name;
        this.fields = fields;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getName() {
        return name;
    }

    public List<Declaration> getFields() {
        return fields;
    }

    /**
     *
     * @param memberName: name of the struct member
     * @return the index of the member in the struct or -1 on failure
     */
    public int locateMember(String memberName) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equals(memberName)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeDeclaration that = (TypeDeclaration) o;
        return Objects.equals(name, that.name) && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fields);
    }

    public String genGlobal(LLVMEnvironment env) {
        StringBuilder fieldTypes = new StringBuilder();
        for (Declaration decl : fields) {
            fieldTypes.append(env.typeToString(decl.getType())).append(", ");
        }
        fieldTypes.delete(fieldTypes.length()-2, fieldTypes.length());
        return String.format("%%%s = type {%s}", name, fieldTypes);
    }
}
