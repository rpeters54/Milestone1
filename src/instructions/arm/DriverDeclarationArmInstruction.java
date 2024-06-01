package instructions.arm;

import java.util.ArrayList;

public class DriverDeclarationArmInstruction extends AbstractArmInstruction {

    public DriverDeclarationArmInstruction(){
        super(new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public String toString() {
        return  ".global println\n"+
                "println:\n"+
                "    .asciz \"%ld\\n\"\n"+
                ".global print\n"+
                "print:\n" +
                "    .asciz \"%ld\"\n"+
                ".global read\n"+
                "read:\n" +
                "    .asciz \"%d\"\n"+
                ".global read_scratch\n"+
                "read_scratch:\n" +
                "    .xword 0x0\n";
    }
}
