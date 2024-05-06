package ast;

public interface BlockHandler {
    BasicBlock toStackBlocks(BasicBlock block, IrFunction func);
    BasicBlock toSSABlocks(BasicBlock block, IrFunction func);
}
