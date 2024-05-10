package instructions;

import java.util.List;

public abstract class AbstractInstruction implements Instruction {
    private Register result;
    private List<Source> sources;


    @Override
    public void substituteSource(Source original, Source replacement) {

    }

    @Override
    public void substituteLabel(Label original, Label replacement) {

    }

}
