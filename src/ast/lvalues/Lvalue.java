package ast.lvalues;

import ast.InstructionHandler;
import ast.Typed;

public interface Lvalue extends Typed, InstructionHandler {
    String getId();
}
