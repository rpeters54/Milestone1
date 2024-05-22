package ast.types;

public interface Typed {
    public Type typecheck(TypeEnvironment env) throws TypeException;
}
