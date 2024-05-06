package ast.statements;

import ast.*;
import ast.lvalues.Lvalue;
import ast.expressions.Expression;
import ast.types.NullType;
import ast.types.StructType;
import ast.types.Type;
import instructions.PhiInstruction;
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
   public BasicBlock toStackBlocks(BasicBlock block, IrFunction func) {
      Source storageLocation = target.toStackInstructions(block, func);
      Source valueToStore = source.toStackInstructions(block, func);

      if (!(storageLocation instanceof Register)) {
         throw new IllegalArgumentException("Can't Store in a Non-Register");
      }
      Register storageRegister = (Register) storageLocation;

      StoreInstruction store = new StoreInstruction(storageRegister, valueToStore);
      block.addCode(store);

      return block;
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      Source storageLocation = target.toSSAInstructions(block, func);
      Source valueToStore = source.toSSAInstructions(block, func);

      // store array and struct members
      if (storageLocation instanceof Register) {
         Register storageRegister = (Register) storageLocation;
         StoreInstruction store = new StoreInstruction(storageRegister, valueToStore);
         block.addCode(store);
         return block;
      }

      // check whether the target is a local or a global
      if (func.isBound(target.getId())) {
         // if it's a local add it to the block bindings
         block.addLocalBinding(target.getId(), valueToStore);
         return block;
      }

      Register globalLookup = func.lookupGlobal(target.getId());
      // if it's a global store it like usual
      if (globalLookup != null) {
         StoreInstruction store = new StoreInstruction(globalLookup, valueToStore);
         block.addCode(store);
         return block;
      }

      throw new IllegalArgumentException("Assign.toSSABlocks(): failed to find bound value");
   }


}
