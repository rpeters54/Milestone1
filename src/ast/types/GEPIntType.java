package ast.types;

import java.util.Objects;

public class GEPIntType implements Type {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof GEPIntType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode("GEP");
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public Type copy() {
        try {
            return (Type) clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
