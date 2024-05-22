package instructions.llvm;

import ast.types.*;

public class TypeMap {
    public static String ttos(Type type) {
        if (type instanceof IntType) {
            return "i64";
        } else if (type instanceof ArrayType){
            return "i64*";
        } else if (type instanceof BoolType) {
            return "i1";
        } else if (type instanceof VoidType) {
            return "void";
        } else if (type instanceof NullType) {
            return "i8*";
        } else if (type instanceof GEPIntType) {
            return "i32";
        } else if (type instanceof PointerType){
            PointerType p = (PointerType) type;
            return ttos(p.getBaseType())+"*";
        } else if (type instanceof FunctionType) {
            FunctionType f = (FunctionType) type;
            return ttos(f.getOutput());
        } else if (type instanceof StructType) {
            StructType s = (StructType) type;
            return String.format("%%struct.%s*", s.getName());
        } else if (type instanceof ArrayAllocType) {
            ArrayAllocType arr = (ArrayAllocType) type;
            return String.format("[%s x i64]", arr.getSize());
        } else {
            throw new IllegalArgumentException("Non-Existent Type");
        }
    }

    public static String deref(String typeString) {
        return typeString.substring(0,typeString.length()-1);
    }
}
