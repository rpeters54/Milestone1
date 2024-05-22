package ast.lvalues;

import ast.InstructionHandler;
import ast.types.Typed;

public interface Lvalue extends Typed, InstructionHandler {
    String getId();
}
