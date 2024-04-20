package ast.expressions;

import ast.*;
import ast.expressions.AbstractExpression;
import ast.types.StructType;
import ast.types.Type;

public class NewExpression
   extends AbstractExpression
{
   private final String id;

   public NewExpression(int lineNum, String id)
   {
      super(lineNum);
      this.id = id;
   }


   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      Type type = env.lookup(id);
      if (!(type instanceof StructType)) {
         throw new TypeException(String.format("NewExpression: Can't " +
                 "Alloc Non-Struct, line: %d", getLineNum()));
      }
      return type;
   }

   @Override
   public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
      return null;
   }
}
