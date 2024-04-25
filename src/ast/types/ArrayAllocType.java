package ast.types;

public class ArrayAllocType implements Type {

    private final String size;

    public ArrayAllocType(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArrayAllocType;
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
