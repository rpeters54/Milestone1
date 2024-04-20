package ast.types;

import java.util.Objects;

public class StructType
   implements Type
{
   private final int lineNum;
   private final String name;

   public StructType(int lineNum, String name)
   {
      this.lineNum = lineNum;
      this.name = name;
   }

   public String getName() {
      return name;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof StructType)) {
         return false;
      }

      StructType type = (StructType) obj;
      return this.name.equals(type.name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(name);
   }
}
