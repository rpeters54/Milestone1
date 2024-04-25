package instructions;

import ast.types.Type;

public interface Source {
    String getValue();
    Type getType();
    String getTypeString();
}
