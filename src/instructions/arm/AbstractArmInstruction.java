package instructions.arm;

import instructions.Label;
import instructions.Register;
import instructions.Source;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractArmInstruction implements ArmInstruction {

    private List<Register> results;
    private List<Source> sources;


    public AbstractArmInstruction(List<Register> results, List<Source> sources) {
        this.results = results;
        this.sources = sources;
    }

    public AbstractArmInstruction(List<Register> results, Source left, Source right) {
        this.results = results;
        this.sources = Arrays.asList(left, right);
    }

    public AbstractArmInstruction(List<Register> results, Source op) {
        this.results = results;
        this.sources = Arrays.asList(op);
    }

    public AbstractArmInstruction(Register result, List<Source> sources) {
        this.results = Arrays.asList(result);
        this.sources = sources;
    }

    public AbstractArmInstruction(Register result, Source left, Source right) {
        this.results = Arrays.asList(result);
        this.sources = Arrays.asList(left, right);
    }

    public AbstractArmInstruction(Register result, Source op) {
        this.results = Arrays.asList(result);
        this.sources = Arrays.asList(op);
    }

    public List<Register> getResults() {
        return results;
    }

    public Register getResultByIndex(int i) {
        return results.get(i);
    }

    @Override
    public Register getResult() {
        return results.get(0);
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    @Override
    public List<Source> getSources() {
        return sources;
    }

    public Source getSource(int i) {
        return sources.get(i);
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).equals(original)) {
                if (!(replacement instanceof Register)) {
                    throw new RuntimeException("AbstractArmInstruction: Tried to replace necessary Register with Source");
                }
                results.set(i, (Register) replacement);
            }
        }
        for (int i = 0; i < sources.size(); i++) {
            if (sources.get(i).equals(original)) {
                sources.set(i, replacement);
            }
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        for (Register result : results) {
            if (original.equals(result.getLabel())) {
                result.setLabel(replacement);
            }
        }
        for (Source source : sources) {
            if (original.equals(source.getLabel())) {
                source.setLabel(replacement);
            }
        }
    }
}
