package ast.expressions;

import ast.*;
import ast.types.BoolType;
import ast.types.IntType;
import ast.types.Type;

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

   public static enum Operator
   {
      TIMES, DIVIDE, PLUS, MINUS, LT, GT, LE, GE, EQ, NE, AND, OR
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
         case LT, LE, GT, GE, EQ, NE -> {
            if (lType instanceof IntType && rType instanceof IntType) {
               return new BoolType();
            }
            throw new TypeException(String.format("Binary Expression: Operands " +
                    "Wrong Type for Arithmetic Comparison, line: %d", getLineNum()));
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
   public Value genInst(BasicBlock block, LLVMEnvironment env) {
      Value leftData = left.genInst(block,env);
      Value rightData = right.genInst(block,env);
      String reg = env.getNextReg();
      String opName = switch (operator) {
         case TIMES -> "mul";
         case DIVIDE -> "sdiv";
         case PLUS -> "add";
         case MINUS -> "sub";
         case LT -> "slt";
         case GT -> "sgt";
         case LE -> "sle";
         case GE -> "sge";
         case EQ -> "eq";
         case NE -> "ne";
         case AND -> "and";
         case OR -> "or";
      };

      // print instruction output based on the operator
      switch (operator) {
         case TIMES, DIVIDE, PLUS, MINUS -> {
            // format binary expression string
            String binop = String.format("%s = %s %s %s, %s", reg, opName,
                    leftData.getIrType(), leftData.getValue(), rightData.getValue());
            // add it to the basic block
            block.addCode(binop);
            return new Value(env, new IntType(), reg);
         }
         case LT, LE, GT, GE, EQ, NE -> {
            // format binary expression string
            String cmp = String.format("%s = icmp %s %s %s, %s",
                    reg, opName, leftData.getIrType(), leftData.getValue(), rightData.getValue());
            // add it to the basic block
            block.addCode(cmp);
            return new Value(env, new BoolType(), reg);
         }
         case AND, OR -> {
            // format binary expression string
            String binop = String.format("%s = %s %s %s, %s", reg, opName,
                    leftData.getIrType(), leftData.getValue(), rightData.getValue());
            // add it to the basic block
            block.addCode(binop);
            return new Value(env, new BoolType(), reg);
         }
         default -> throw new IllegalArgumentException("Binary Expression: Failed to Resolve Binop");
      }
   }
}
