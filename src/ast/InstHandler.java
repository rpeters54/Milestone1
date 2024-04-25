package ast;

import instructions.Source;

public interface InstHandler {
    Source genInst(BasicBlock block, LLVMEnvironment env);
}
