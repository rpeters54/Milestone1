package ast;

import ast.types.Type;
import instructions.Label;
import instructions.Source;

public class PhiTuple {
    private Source source;
    private Label origin;

    public PhiTuple(Source source, Label origin) {
        this.source = source;
        this.origin = origin;
    }

    public Type getType() {
        return source.getType();
    }

    public Source getSource() {
        return source;
    }

    public Label getOrigin() {
        return origin;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public void setOrigin(Label origin) {
        this.origin = origin;
    }
}
