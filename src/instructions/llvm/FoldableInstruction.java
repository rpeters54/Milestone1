package instructions.llvm;

import instructions.Literal;
import instructions.Register;

public interface FoldableInstruction {
    Literal fold();
    Register getResult();
}
