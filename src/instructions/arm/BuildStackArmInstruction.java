package instructions.arm;

import ast.IrFunction;

import java.util.ArrayList;

public class BuildStackArmInstruction extends AbstractArmInstruction{

    private int size;

    public BuildStackArmInstruction() {
        super(new ArrayList<>(), new ArrayList<>());
        this.size = IrFunction.baseSize;
    }

    public void setExtraSpace(int space) {
        this.size = space;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(String.format("stp x29, x30, [sp, -%d]!\n", size));
        int offset = 16;
        for (int i = 19; i < 29; i+=2) {
            builder.append(String.format("stp x%d, x%d, [sp, %d]\n", i, i+1, offset));
            offset += 16;
        }
        return builder.toString();
    }
}
