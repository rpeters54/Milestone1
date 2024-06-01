package ast.declarations;

import ast.types.*;

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

    public int getLLVMSize() {
        int size = 0;
        for (Declaration decl : fields) {
            Type type = decl.getType();
            if (type instanceof BoolType) {
                size += 1;
            } else {
                size += 8;
            }
        }
        return size;
    }

    public int getArmSize() {
        return fields.size() * 8;
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
}
