package ast;

public interface InstHandler {
    public Value genInst(BasicBlock block, LLVMEnvironment env);
}
