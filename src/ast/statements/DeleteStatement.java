package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.*;
import instructions.*;

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
      return evalDelete(block, deleteItem);
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      Source deleteItem = expression.toSSAInstructions(block, func);
      return evalDelete(block, deleteItem);
   }

   public BasicBlock evalDelete(BasicBlock block, Source deleteItem) {
      Register castResult = Register.genTypedLocalRegister(new NullType(), block.getLabel());

      BitcastInstruction cast = new BitcastInstruction(castResult, deleteItem);
      FreeCallInstruction call = new FreeCallInstruction(castResult);

      block.addCode(cast);
      block.addCode(call);

      return block;
   }
}
