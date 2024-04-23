package ast.statements;

import ast.BlockHandler;
import ast.InstHandler;
import ast.Typed;

public interface Statement extends Typed, BlockHandler {
    public boolean alwaysReturns();
}
