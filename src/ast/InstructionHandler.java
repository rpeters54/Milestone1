package ast;

import instructions.Source;

public interface InstructionHandler {
    Source toStackInstructions(BasicBlock block, IrFunction func);
    Source toSSAInstructions(BasicBlock block, IrFunction func);
}
