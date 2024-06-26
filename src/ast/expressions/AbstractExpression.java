package ast.expressions;

public abstract class AbstractExpression
   implements Expression
{
   private final int lineNum;

   public AbstractExpression(int lineNum)
   {
      this.lineNum = lineNum;
   }

   public int getLineNum() {
      return lineNum;
   }
}
