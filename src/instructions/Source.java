package instructions;

import ast.types.Type;

public interface Source {
    String getValue();
    Type getType();
    Label getLabel();
    void setLabel(Label label);
    String getTypeString();
    Source copy();
}
