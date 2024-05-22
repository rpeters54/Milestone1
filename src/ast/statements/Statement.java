package ast.statements;

import ast.BlockHandler;
import ast.types.Typed;

public interface Statement extends Typed, BlockHandler {
    boolean alwaysReturns();
}
