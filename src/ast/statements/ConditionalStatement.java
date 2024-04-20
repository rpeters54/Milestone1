package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.statements.AbstractStatement;
import ast.statements.Statement;
import ast.types.BoolType;
import ast.types.Type;
import ast.types.VoidType;

public class ConditionalStatement
   extends AbstractStatement
{
   private final Expression guard;
   private final Statement thenBlock;
   private final Statement elseBlock;

   public ConditionalStatement(int lineNum, Expression guard,
      Statement thenBlock, Statement elseBlock)
   {
      super(lineNum);
      this.guard = guard;
      this.thenBlock = thenBlock;
      this.elseBlock = elseBlock;
   }


   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      /* guard must be a boolean */
      if (!(guard.typecheck(env) instanceof BoolType)) {
         throw new TypeException(String.format("ConditionalStatement: Non-Boolean " +
                 "Guard, line: %d", getLineNum()));
      }

      /* ensure both cases properly type check */
      thenBlock.typecheck(env);
      elseBlock.typecheck(env);

      /* return void on success */
      return new VoidType();
   }

   @Override
   public boolean alwaysReturns() {
      return thenBlock.alwaysReturns() && elseBlock.alwaysReturns();
   }

   @Override
   public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {

   }
}
