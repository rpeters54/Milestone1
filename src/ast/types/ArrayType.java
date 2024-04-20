package ast.types;

public class ArrayType implements Type {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArrayType;
    }

}
