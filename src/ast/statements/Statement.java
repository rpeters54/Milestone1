package ast.statements;

import ast.Codegen;
import ast.Typed;

public interface Statement extends Typed, Codegen {
    public boolean alwaysReturns();
}
