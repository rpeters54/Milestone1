package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.*;
import instructions.*;
import instructions.llvm.BitcastLLVMInstruction;
import instructions.llvm.FreeCallLLVMInstruction;

public class DeleteStatement
   extends AbstractStatement
{
   private final Expression expression;

   public DeleteStatement(int lineNum, Expression expression)
   {
      super(lineNum);
      this.expression = expression;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      /* if the expression is a struct type that has been declared return it */
      Type type = expression.typecheck(env);
      if (type instanceof StructType || type instanceof ArrayType) {
         return type;
      }
      throw new TypeException(String.format("DeleteStatement: Invalid " +
              "Structure Type, line: %d", getLineNum()));
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }

   @Override
   public BasicBlock toStackBlocks(BasicBlock block, IrFunction func) {
      Source deleteItem = expression.toStackInstructions(block, func);
      return evalDelete(block, func, deleteItem);
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      Source deleteItem = expression.toSSAInstructions(block, func);
      return evalDelete(block, func, deleteItem);
   }

   public BasicBlock evalDelete(BasicBlock block, IrFunction func, Source deleteItem) {
      Register castResult = Register.genTypedLocalRegister(new NullType(), block.getLabel());

      BitcastLLVMInstruction cast = new BitcastLLVMInstruction(castResult, deleteItem);
      FreeCallLLVMInstruction call = new FreeCallLLVMInstruction(castResult);

      block.addCode(cast);
      block.addCode(call);

      return block;
   }
}
