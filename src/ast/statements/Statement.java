package ast.statements;

import ast.BlockHandler;
import ast.Typed;

public interface Statement extends Typed, BlockHandler {
    boolean alwaysReturns();
}
