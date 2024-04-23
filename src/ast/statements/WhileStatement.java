package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.BoolType;
import ast.types.Type;
import ast.types.VoidType;

public class WhileStatement
   extends AbstractStatement
{
   private final Expression guard;
   private final Statement body;

   public WhileStatement(int lineNum, Expression guard, Statement body)
   {
      super(lineNum);
      this.guard = guard;
      this.body = body;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      // guard must be a boolean
      if (!(guard.typecheck(env) instanceof BoolType)) {
         throw new TypeException(String.format("WhileStatement: Non-Boolean " +
                 "Guard, line: %d", getLineNum()));
      }

      body.typecheck(env);

      return new VoidType();
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }

   @Override
   public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {
      Value guardData = guard.genInst(block, env);
      String innerLabel = env.getNextBranch();
      String outerLabel = env.getNextBranch();
      block.addCode(LLVMPrinter.condBranch(guardData, innerLabel, outerLabel));
      BasicBlock inner = new BasicBlock();
      block.addChild(inner);
      inner.addCode(LLVMPrinter.label(innerLabel));
      BasicBlock lastInner = body.genBlock(inner, env);
      lastInner.addCode(LLVMPrinter.condBranch(guardData, innerLabel, outerLabel));
      BasicBlock outer = new BasicBlock();
      lastInner.addChild(outer);
      outer.addCode(LLVMPrinter.label(outerLabel));
      return outer;
   }
}
