package ast.statements;

import ast.*;
import ast.lvalues.Lvalue;
import ast.expressions.Expression;
import ast.types.NullType;
import ast.types.StructType;
import ast.types.Type;
import instructions.Register;
import instructions.Source;
import instructions.StoreInstruction;

public class AssignmentStatement
   extends AbstractStatement {
   private final Lvalue target;
   private final Expression source;

   public AssignmentStatement(int lineNum, Lvalue target, Expression source) {
      super(lineNum);
      this.target = target;
      this.source = source;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      Type left = target.typecheck(env);
      Type right = source.typecheck(env);

      if (left.equals(right)) {
         return left;
      }

      if (left instanceof StructType && right instanceof NullType) {
         return left;
      }

      throw new TypeException(String.format("AssignmentStatement: Left and " +
              "Right Operand Do Not Match, line: %d", getLineNum()));
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }

   @Override
   public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {
      Source storageLocation = target.genInst(block, env);
      Source valueToStore = source.genInst(block, env);

      if (!(storageLocation instanceof Register)) {
         throw new IllegalArgumentException("Can't Store in a Non-Register");
      }
      Register storageRegister = (Register) storageLocation;

      StoreInstruction store = new StoreInstruction(storageRegister, valueToStore);
      block.addCode(store);

      return block;
   }
}
