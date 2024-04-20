package ast;

import ast.types.Type;

import java.util.Map;

public interface Codegen {
    public LLVMMetadata genLLVM(BasicBlock block,
                                LLVMEnvironment env);
}
