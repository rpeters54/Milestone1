package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.BoolType;
import ast.types.Type;
import ast.types.VoidType;
import instructions.ConditionalBranchInstruction;
import instructions.Label;
import instructions.Source;

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
      // generate code for the guard
      Source guardData = guard.genInst(block, env);

      // generate labels and blocks
      Label innerStub = new Label();
      Label outerStub = new Label();
      BasicBlock inner = new BasicBlock();
      BasicBlock outer = new BasicBlock();

      // add the conditional branch to the inner and outer blocks to the parent
      ConditionalBranchInstruction cond = new ConditionalBranchInstruction(guardData, innerStub, outerStub);
      block.addCode(cond);

      // add the body and after blocks as children of the parent
      block.addChild(inner);
      block.addChild(outer);

      // add label to the inner block
      inner.addCode(innerStub);

      // evaluate the body blocks
      BasicBlock lastInner = body.genBlock(inner, env);

      //if the body ends with a return/jump to somewhere else,
      // dont put a branch to the outer at the end
      if (!lastInner.endsWithJump()) {
         lastInner.addCode(cond);
         lastInner.addChild(outer);
      }

      // add the label to the outer stub
      outer.addCode(outerStub);
      return outer;
   }
}
