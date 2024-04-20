package ast.types;

public class IntType implements Type {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntType;
    }
}
