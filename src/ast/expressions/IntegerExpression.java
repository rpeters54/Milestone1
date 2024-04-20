package ast.expressions;

import ast.*;
import ast.types.IntType;
import ast.types.Type;

public class IntegerExpression
   extends AbstractExpression
{
   private final String value;

   public IntegerExpression(int lineNum, String value)
   {
      super(lineNum);
      this.value = value;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new IntType();
   }


   @Override
   public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
      Type type = new IntType();
      return new LLVMMetadata(type, env.typeToString(type), value);
   }
}
