package ast.expressions;

import ast.*;
import ast.types.*;
import instructions.BinaryInstruction;
import instructions.ComparatorInstruction;
import instructions.Register;
import instructions.Source;

public class BinaryExpression
   extends AbstractExpression
{
   private final Operator operator;
   private final Expression left;
   private final Expression right;

   private BinaryExpression(int lineNum, Operator operator,
      Expression left, Expression right)
   {
      super(lineNum);
      this.operator = operator;
      this.left = left;
      this.right = right;
   }

   public static BinaryExpression create(int lineNum, String opStr,
      Expression left, Expression right)
   {
      return switch (opStr) {
         case TIMES_OPERATOR -> new BinaryExpression(lineNum, Operator.TIMES, left, right);
         case DIVIDE_OPERATOR -> new BinaryExpression(lineNum, Operator.DIVIDE, left, right);
         case PLUS_OPERATOR -> new BinaryExpression(lineNum, Operator.PLUS, left, right);
         case MINUS_OPERATOR -> new BinaryExpression(lineNum, Operator.MINUS, left, right);
         case LT_OPERATOR -> new BinaryExpression(lineNum, Operator.LT, left, right);
         case LE_OPERATOR -> new BinaryExpression(lineNum, Operator.LE, left, right);
         case GT_OPERATOR -> new BinaryExpression(lineNum, Operator.GT, left, right);
         case GE_OPERATOR -> new BinaryExpression(lineNum, Operator.GE, left, right);
         case EQ_OPERATOR -> new BinaryExpression(lineNum, Operator.EQ, left, right);
         case NE_OPERATOR -> new BinaryExpression(lineNum, Operator.NE, left, right);
         case AND_OPERATOR -> new BinaryExpression(lineNum, Operator.AND, left, right);
         case OR_OPERATOR -> new BinaryExpression(lineNum, Operator.OR, left, right);
         default -> throw new IllegalArgumentException();
      };
   }

   private static final String TIMES_OPERATOR = "*";
   private static final String DIVIDE_OPERATOR = "/";
   private static final String PLUS_OPERATOR = "+";
   private static final String MINUS_OPERATOR = "-";
   private static final String LT_OPERATOR = "<";
   private static final String LE_OPERATOR = "<=";
   private static final String GT_OPERATOR = ">";
   private static final String GE_OPERATOR = ">=";
   private static final String EQ_OPERATOR = "==";
   private static final String NE_OPERATOR = "!=";
   private static final String AND_OPERATOR = "&&";
   private static final String OR_OPERATOR = "||";

   public enum Operator
   {
      TIMES, DIVIDE, PLUS, MINUS, LT, GT, LE, GE, EQ, NE, AND, OR, XOR //added for unary
   }


   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      Type lType = left.typecheck(env);
      Type rType = right.typecheck(env);
      switch (operator) {
         case TIMES, DIVIDE, PLUS, MINUS -> {
            if (lType instanceof IntType && rType instanceof IntType) {
               return lType;
            }
            throw new TypeException(String.format("Binary Expression: Operands " +
                    "Wrong Type for Arithmetic Expression, line: %d", getLineNum()));
         }
         case LT, LE, GT, GE -> {
            if (lType instanceof IntType && rType instanceof IntType) {
               return new BoolType();
            }
            throw new TypeException(String.format("Binary Expression: Operands " +
                    "Wrong Type for Arithmetic Comparison, line: %d", getLineNum()));
         }
         case EQ, NE -> {
            Boolean lNullCheck = lType instanceof StructType || lType instanceof NullType;
            Boolean rNullCheck = rType instanceof StructType || rType instanceof NullType;

            if (lType instanceof IntType && rType instanceof IntType
            || lNullCheck && rNullCheck){
               return new BoolType();
            }
            throw new TypeException(String.format("Binary Expression: Operands " +
                    "Wrong Type for Equality Comparison, line: %d", getLineNum()));
         }
         case AND, OR -> {
            if (lType instanceof BoolType && rType instanceof BoolType) {
               return lType;
            }
            throw new TypeException(String.format("Binary Expression: Operands " +
                    "Wrong Type for Boolean Expression, line: %d", getLineNum()));
         }
         default -> throw new TypeException(String.format("Binary Expression: " +
                 "Something Went Horribly Wrong, line: %d", getLineNum()));
      }
   }

   @Override
   public Source toStackInstructions(BasicBlock block, IrFunction func) {
      Source leftSource = left.toStackInstructions(block, func);
      Source rightSource = right.toStackInstructions(block, func);
      return evalBinop(block, leftSource, rightSource);
   }

   @Override
   public Source toSSAInstructions(BasicBlock block, IrFunction func) {
      Source leftSource = left.toSSAInstructions(block, func);
      Source rightSource = right.toSSAInstructions(block, func);
      return evalBinop(block, leftSource, rightSource);
   }

   private Source evalBinop(BasicBlock block, Source leftSource, Source rightSource) {
      Register result = Register.genLocalRegister(block.getLabel());

      // print instruction output based on the operator
      switch (operator) {
         case TIMES, DIVIDE, PLUS, MINUS -> {
            // format binary expression string
            BinaryInstruction binop = new BinaryInstruction(result, operator, leftSource, rightSource);
            // add it to the basic block
            block.addCode(binop);
            result.setType(new IntType());
            return result;
         }
         case LT, LE, GT, GE, EQ, NE -> {
            // format binary expression string
            ComparatorInstruction cmp = new ComparatorInstruction(result, operator, leftSource, rightSource);
            // add it to the basic block
            block.addCode(cmp);
            result.setType(new BoolType());
            return result;
         }
         case AND, OR -> {
            // format binary expression string
            BinaryInstruction binop = new BinaryInstruction(result, operator, leftSource, rightSource);
            // add it to the basic block
            block.addCode(binop);
            result.setType(new BoolType());
            return result;
         }
         default -> throw new IllegalArgumentException("Binary Expression: Failed to Resolve Binop");
      }
   }
}
