package ast.statements;

import ast.*;
import ast.lvalues.Lvalue;
import ast.expressions.Expression;
import ast.types.NullType;
import ast.types.StructType;
import ast.types.Type;

public class AssignmentStatement
   extends AbstractStatement {
   private final Lvalue target;
   private final Expression source;

   public AssignmentStatement(int lineNum, Lvalue target, Expression source) {
      super(lineNum);
      this.target = target;
      this.source = source;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      Type left = target.typecheck(env);
      Type right = source.typecheck(env);

      if (left.equals(right)) {
         return left;
      }

      if (left instanceof StructType && right instanceof NullType) {
         return left;
      }

      throw new TypeException(String.format("AssignmentStatement: Left and " +
              "Right Operand Do Not Match, line: %d", getLineNum()));
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }

   @Override
   public LLVMMetadata genLLVM(BasicBlock block,
                               LLVMEnvironment env) {


   }
}
