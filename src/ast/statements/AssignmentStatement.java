package ast.statements;

import ast.*;
import ast.lvalues.Lvalue;
import ast.expressions.Expression;
import ast.types.NullType;
import ast.types.Type;
import ast.types.TypeEnvironment;
import ast.types.TypeException;
import instructions.*;
import instructions.llvm.StoreLLVMInstruction;

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

      if (left instanceof NullType) {
         throw new TypeException(String.format("AssignmentStatement: Can't assign to " +
                 "a null value, line: %d", getLineNum()));
      }

      if (left.equals(right)) {
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

      StoreLLVMInstruction store = new StoreLLVMInstruction(valueToStore, storageRegister);
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
         repairNull(storageRegister.getType(), valueToStore);
         StoreLLVMInstruction store = new StoreLLVMInstruction(valueToStore, storageRegister);

         block.addCode(store);

         return block;
      }

      // check whether the target is a local or a global
      if (func.isBound(target.getId())) {
         // if it's a local add it to the block bindings
         repairNull(func.getTypeOfDeclaration(target.getId()), valueToStore);
         block.addLocalBinding(target.getId(), valueToStore);

         return block;
      }

      Register globalLookup = func.lookupGlobal(target.getId());
      // if it's a global store it like usual
      if (globalLookup != null) {
         repairNull(globalLookup.getType(), valueToStore);
         StoreLLVMInstruction store = new StoreLLVMInstruction(valueToStore, globalLookup);
         block.addCode(store);

         return block;
      }

      throw new RuntimeException("AssignmentStatement::toSSABlocks: failed to find bound value");
   }

   void repairNull(Type type, Source maybeNull) {
      if (maybeNull instanceof Literal && maybeNull.getType() instanceof NullType) {
         maybeNull.setType(type.copy());
      }
   }

}
