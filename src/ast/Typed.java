package ast;

import ast.types.Type;

public interface Typed {
    public Type typecheck(TypeEnvironment env) throws TypeException;
}
