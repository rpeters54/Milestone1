package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.BoolType;
import ast.types.Type;
import ast.types.VoidType;
import instructions.*;

import java.util.*;

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
   public BasicBlock toStackBlocks(BasicBlock block, IrFunction func) {

      // evaluate the guard
      Source guardData = guard.toStackInstructions(block, func);

      // create and traverse true basic block
      Label trueStub = new Label();
      BasicBlock lastTrueBlock = makeStackSubBlock(block, thenBlock,trueStub, func);
      boolean thenReturns = lastTrueBlock.endsWithJump();


      Label falseStub = new Label();
      BasicBlock lastFalseBlock = makeStackSubBlock(block, elseBlock, falseStub, func);
      boolean elseReturns = lastFalseBlock.endsWithJump();

      // print the conditional branch at the end of the original block
      ConditionalBranchInstruction cond = new ConditionalBranchInstruction(guardData, trueStub, falseStub);
      block.addCode(cond);
      if (!(thenReturns && elseReturns)) {
         BasicBlock endBlock = new BasicBlock();
         func.addToQueue(endBlock);
         Label endStub = new Label();
         endBlock.setLabel(endStub);
         if (!thenReturns) {
            UnconditionalBranchInstruction trueToEnd = new UnconditionalBranchInstruction(endStub);
            lastTrueBlock.addCode(trueToEnd);
            lastTrueBlock.addChild(endBlock);
         }
         if (!elseReturns) {
            UnconditionalBranchInstruction falseToEnd = new UnconditionalBranchInstruction(endStub);
            lastFalseBlock.addCode(falseToEnd);
            lastFalseBlock.addChild(endBlock);
         }
         return endBlock;
      } else {
         return lastFalseBlock;
      }
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      Source guardData = guard.toSSAInstructions(block, func);

      // create and traverse true basic block
      Label trueStub = new Label();
      BasicBlock lastTrueBlock = makeSSASubBlock(block, thenBlock, trueStub, func);
      boolean thenReturns = lastTrueBlock.endsWithJump();


      Label falseStub = new Label();
      BasicBlock lastFalseBlock = makeSSASubBlock(block, elseBlock, falseStub, func);
      boolean elseReturns = lastFalseBlock.endsWithJump();



      // print the conditional branch at the end of the original block
      ConditionalBranchInstruction cond = new ConditionalBranchInstruction(guardData, trueStub, falseStub);
      block.addCode(cond);

      if (thenReturns && elseReturns) {
         return lastFalseBlock;
      }

      BasicBlock endBlock = new BasicBlock();
      func.addToQueue(endBlock);
      Label endStub = new Label();
      endBlock.setLabel(endStub);

      if (!thenReturns && elseReturns) {
         UnconditionalBranchInstruction trueToEnd = new UnconditionalBranchInstruction(endStub);
         lastTrueBlock.addCode(trueToEnd);
         lastTrueBlock.addChild(endBlock);
      }

      if (!elseReturns && thenReturns) {
         UnconditionalBranchInstruction falseToEnd = new UnconditionalBranchInstruction(endStub);
         lastFalseBlock.addCode(falseToEnd);
         lastFalseBlock.addChild(endBlock);
      }

      if (!elseReturns && !thenReturns) {
         UnconditionalBranchInstruction trueToEnd = new UnconditionalBranchInstruction(endStub);
         lastTrueBlock.addCode(trueToEnd);
         lastTrueBlock.addChild(endBlock);
         UnconditionalBranchInstruction falseToEnd = new UnconditionalBranchInstruction(endStub);
         lastFalseBlock.addCode(falseToEnd);
         lastFalseBlock.addChild(endBlock);
         endBlock.reconcileBranch(lastTrueBlock, lastFalseBlock);
      }

      return endBlock;
   }


   public BasicBlock makeStackSubBlock(BasicBlock parentBlock, Statement elif, Label subLabel, IrFunction func) {
      // create the then basic block and add its label
      BasicBlock subBlock = new BasicBlock();
      func.addToQueue(subBlock);
      subBlock.setLabel(subLabel);

      // add the true block to the list of the first block's children
      parentBlock.addChild(subBlock);

      // evaluate the internals of the true block, possibly generating more blocks
      // keep track of the last block made
      return elif.toStackBlocks(subBlock, func);
   }

   public BasicBlock makeSSASubBlock(BasicBlock parentBlock, Statement elif, Label subLabel, IrFunction func) {
      // create the child basic block and add its label
      BasicBlock subBlock = new BasicBlock();

      func.addToQueue(subBlock);
      subBlock.setLabel(subLabel);

      // add the true block to the list of the first block's children
      parentBlock.addChild(subBlock);

      // evaluate the internals of the true block, possibly generating more blocks
      // keep track of the last block made
      return elif.toSSABlocks(subBlock, func);
   }


}
