package ast.types;

public class BoolType implements Type {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BoolType;
    }

}
