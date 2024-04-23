package ast.statements;

import ast.*;
import ast.types.Type;
import ast.types.VoidType;

import java.util.List;
import java.util.ArrayList;

public class BlockStatement
   extends AbstractStatement
{
   private final List<Statement> statements;

   public BlockStatement(int lineNum, List<Statement> statements)
   {
      super(lineNum);
      this.statements = statements;
   }

   public static BlockStatement emptyBlock()
   {
      return new BlockStatement(-1, new ArrayList<>());
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      Type type = new VoidType();
      for (Statement statement : statements) {
         type = statement.typecheck(env);
      }
      return type;
   }

   @Override
   public boolean alwaysReturns() {
      for (Statement statement : statements) {
         if (statement.alwaysReturns()) {
            return true;
         }
      }
      return false;
   }

   @Override
   public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {
      BasicBlock tmp = block;
      for (Statement stmt : statements) {
         tmp = stmt.genBlock(tmp,env);
      }
      return tmp;
   }
}
