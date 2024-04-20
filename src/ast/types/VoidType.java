package ast.types;

public class VoidType implements Type {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoidType;
    }
}
