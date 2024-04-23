package ast;

public interface BlockHandler {
    public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env);
}
