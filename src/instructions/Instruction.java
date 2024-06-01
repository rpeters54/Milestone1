package instructions;

import java.util.List;

public interface Instruction {
    // for replacing sources and labels during transformations
    void substituteSource(Source original, Source replacement);
    void substituteLabel(Label original, Label replacement);

    // getters (self explanatory)
    Register getResult();
    List<Source> getSources();

    List<Register> getResults();
}
