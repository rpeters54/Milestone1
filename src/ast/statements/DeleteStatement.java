package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.*;
import instructions.*;

import java.util.ArrayList;
import java.util.List;

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
   public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {
      Source deleteItem = expression.genInst(block, env);
      Register freeResult = new Register(new VoidType());

      FreeCallInstruction call = new FreeCallInstruction(freeResult, deleteItem);
      block.addCode(call);

      return block;
   }
}
