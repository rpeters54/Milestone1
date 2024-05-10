package instructions;

public interface FoldableInstruction extends Instruction {
    Literal fold();
    Register getResult();
}
