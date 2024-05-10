package instructions;

public interface Instruction {
    void substituteSource(Source original, Source replacement);
    void substituteLabel(Label original, Label replacement);
}
