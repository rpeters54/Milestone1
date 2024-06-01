package instructions.arm;

import ast.IrFunction;

import java.util.ArrayList;

public class DestroyStackArmInstruction extends AbstractArmInstruction {

    private int size;

    public DestroyStackArmInstruction() {
        super(new ArrayList<>(), new ArrayList<>());
        this.size = IrFunction.baseSize;
    }

    public void setExtraSpace(int space) {
        this.size = space;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int offset = IrFunction.baseSize;
        for (int i = 28; i >= 19; i-=2) {
            offset -= 16;
            builder.append(String.format("ldp x%d, x%d, [sp, %d]\n", i-1, i, offset));
        }
        builder.append(String.format("ldp x29, x30, [sp], %d", size));
        return builder.toString();
    }
}
