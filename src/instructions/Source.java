package instructions;

import ast.types.Type;

public interface Source {
    Type getType();
    void setType(Type type);
    Label getLabel();
    void setLabel(Label label);
    String getTypeString();
    Source copy();
}
