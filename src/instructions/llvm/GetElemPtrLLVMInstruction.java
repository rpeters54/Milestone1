package instructions.llvm;

import ast.expressions.BinaryExpression;
import ast.types.ArrayType;
import ast.types.IntType;
import ast.types.StructType;
import instructions.Literal;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.BinaryArmInstruction;

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
                    getResult(), deref, obj().getTypeString(), obj(), index());
        }
        if (obj().getType() instanceof ArrayType) {
            return String.format("%s = getelementptr inbounds %s, %s %s, i64 %s",
                    getResult(), deref, obj().getTypeString(), obj(), index());
        }
        throw new RuntimeException("GetElemPtrLLVMInstruction::toString: Can't create gep instruction with non-array or" +
                "non-struct type");
    }

    @Override
    public List<ArmInstruction> toArm() {
        List<ArmInstruction> instList = new ArrayList<>();
        if (!(index() instanceof Literal))
            throw new RuntimeException("GetElemPtrLLVMInstruction::toArm: index not a literal," +
                    " so pointer offset can't be computed");
        String offsetValue = Integer.toString(8*Integer.parseInt(index().toString()));
        Literal offset = new Literal(new IntType(),offsetValue, index().getLabel());
        instList.add(new BinaryArmInstruction(getResult(), BinaryExpression.Operator.PLUS, obj(), offset));
        return instList;
    }
}
