package ast.expressions;

import ast.*;
import ast.expressions.AbstractExpression;
import ast.types.NullType;
import ast.types.Type;

public class NullExpression extends AbstractExpression {
   public NullExpression(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new NullType();
   }

   @Override
   public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
      Type type = new NullType();
      return new LLVMMetadata(type, env.typeToString(type), "null");
   }
}




