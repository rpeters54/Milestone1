package instructions.llvm;

import ast.BasicBlock;
import instructions.Label;
import instructions.Register;
import instructions.Source;

import java.util.Collections;
import java.util.List;

public abstract class AbstractLLVMInstruction implements LLVMInstruction {
    private Register result;
    private List<Source> sources;
    private BasicBlock block;
    private boolean deathmark;

    public AbstractLLVMInstruction(Register result, List<Source> sources) {
        this.result = result;
        this.sources = sources;
        this.block = null;
        this.deathmark = true;
    }


    @Override
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(result)) {
            if (replacement instanceof Register) {
                result = (Register) replacement;
            }
            throw new RuntimeException("AbstractLLVMInstruction: Tried to replace necessary Register with Source");
        }
        for (int i = 0; i < sources.size(); i++) {
            if (sources.get(i).equals(original)) {
                sources.set(i, replacement);
            }
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (result != null && original.equals(result.getLabel())) {
            result.setLabel(replacement);
        }
        for (Source source : sources) {
            if (original.equals(source.getLabel())) {
                source.setLabel(replacement);
            }
        }
    }

    public Register getResult() {
        return result;
    }

    @Override
    public List<Register> getResults() {
        return Collections.singletonList(result);
    }

    public void setResult(Register result) {
        this.result = result;
    }

    public List<Source> getSources() {
        return sources;
    }

    public Source getSource(int index) {
        return sources.get(index);
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public void setSource(int index, Source source) {
        this.sources.set(index, source);
    }

    public BasicBlock getBlock() {
        return block;
    }

    public void setBlock(BasicBlock block) {
        this.block = block;
    }

    public void addSource(Source source) {
        sources.add(source);
    }

    @Override
    public void markCritical() {
        deathmark = false;
    }

    @Override
    public void markDead() {
        deathmark = true;
    }

    @Override
    public boolean getDeathMark() {
        return deathmark;
    }
}
