package ast.expressions;

import ast.*;
import ast.types.IntType;
import ast.types.Type;

public class ReadExpression
   extends AbstractExpression
{
   public ReadExpression(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new IntType();
   }

   @Override
   public Value genInst(BasicBlock block, LLVMEnvironment env) {
      block.addCode("call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([4 x i8]* @.read, i32 0, i32 0), i64* @.read_scratch)");
      String reg = env.getNextReg();
      block.addCode(String.format("%s = load i64* @.read_scratch", reg));
      return new Value(env, new IntType(), reg);
   }
}


