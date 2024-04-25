package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.BoolType;
import ast.types.Type;
import ast.types.VoidType;
import instructions.*;

import java.util.List;

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
      if (elseBlock != null) {
         elseBlock.typecheck(env);
      }

      /* return void on success */
      return new VoidType();
   }

   @Override
   public boolean alwaysReturns() {
      if (elseBlock != null) {
         return thenBlock.alwaysReturns() && elseBlock.alwaysReturns();
      }
      return thenBlock.alwaysReturns();
   }

   @Override
   public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {

      // evaluate the guard
      Source guardData = guard.genInst(block, env);

      // create and traverse true basic block
      Label trueStub = new Label();
      BasicBlock lastTrueBlock = makeSubBlock(block, trueStub, env);
      boolean thenReturns = lastTrueBlock.endsWithJump();

      BasicBlock endBlock = new BasicBlock();

      if (elseBlock != null) {
         Label falseStub = new Label();
         BasicBlock lastFalseBlock = makeSubBlock(block, falseStub, env);
         boolean elseReturns = lastFalseBlock.endsWithJump();
         // print the conditional branch at the end of the original block
         ConditionalBranchInstruction cond = new ConditionalBranchInstruction(guardData, trueStub, falseStub);
         block.addCode(cond);
         if (!(thenReturns && elseReturns)) {
            Label endStub = new Label();
            endBlock.addCode(endStub);
            UnconditionalBranchInstruction trueToEnd = new UnconditionalBranchInstruction(endStub);
            lastTrueBlock.addCode(trueToEnd);
            lastTrueBlock.addChild(endBlock);
            UnconditionalBranchInstruction falseToEnd = new UnconditionalBranchInstruction(endStub);
            lastFalseBlock.addCode(falseToEnd);
            lastFalseBlock.addChild(endBlock);
            return endBlock;
         }
         return lastFalseBlock;
      } else {
         Label endStub = new Label();
         endBlock.addCode(endStub);
         UnconditionalBranchInstruction trueToEnd = new UnconditionalBranchInstruction(endStub);
         lastTrueBlock.addCode(trueToEnd);
         lastTrueBlock.addChild(endBlock);
         // with no else block, false branches to the end block instead
         ConditionalBranchInstruction cond = new ConditionalBranchInstruction(guardData, trueStub, endStub);
         block.addCode(cond);
         return endBlock;
      }
   }








   public BasicBlock makeSubBlock(BasicBlock parentBlock, Label subLabel, LLVMEnvironment env) {
      // create the then basic block and add its label
      BasicBlock subBlock = new BasicBlock();
      subBlock.addCode(subLabel);

      // add the true block to the list of the first block's children
      parentBlock.addChild(subBlock);

      // evaluate the internals of the true block, possibly generating more blocks
      // keep track of the last block made
      return thenBlock.genBlock(subBlock, env);
   }


}
