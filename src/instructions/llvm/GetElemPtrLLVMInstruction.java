package instructions.llvm;

import ast.expressions.BinaryExpression;
import ast.types.ArrayType;
import ast.types.IntType;
import ast.types.StructType;
import instructions.Instruction;
import instructions.Literal;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.BinaryArmInstruction;
import instructions.arm.MovArmInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetElemPtrLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {

    public GetElemPtrLLVMInstruction(Register result, Source obj, Source index) {
        super(result, new ArrayList<>(Arrays.asList(obj, index)));
    }


    private Source obj() {
        return super.getSource(0);
    }

    private Source index() {
        return super.getSource(1);
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(obj().getTypeString());
        if (obj().getType() instanceof StructType) {
            return String.format("%s = getelementptr inbounds %s, %s %s, i32 0, i32 %s",
                    getResult().llvmString(), deref, obj().getTypeString(), obj().llvmString(), index().llvmString());
        }
        if (obj().getType() instanceof ArrayType) {
            return String.format("%s = getelementptr inbounds %s, %s %s, i64 %s",
                    getResult().llvmString(), deref, obj().getTypeString(), obj().llvmString(), index().llvmString());
        }
        throw new RuntimeException("GetElemPtrLLVMInstruction::toString: Can't create gep instruction with non-array or" +
                "non-struct type");
    }

    @Override
    public List<Instruction> toArm() {
        List<Instruction> instList = new ArrayList<>();
        Register offset = Register.genTypedLocalRegister(new IntType(), getResult().getLabel());
        Register tempIndex = Register.genTypedLocalRegister(new IntType(), getResult().getLabel());
        Register eightContainer = Register.genTypedLocalRegister(new IntType(), getResult().getLabel());

        instList.add(new MovArmInstruction(tempIndex, index()));
        instList.add(new MovArmInstruction(eightContainer, new Literal(new IntType(), "8", getResult().getLabel())));
        // generate a multiplication instruction to compute the offset
        instList.add(new BinaryArmInstruction(offset, BinaryExpression.Operator.TIMES, tempIndex, eightContainer));
        // generate the final pointer by adding the offset to the principal pointer
        instList.add(new BinaryArmInstruction(getResult(), BinaryExpression.Operator.PLUS, obj(), offset));
        return instList;
    }
}
