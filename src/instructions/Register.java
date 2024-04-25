package instructions;

import ast.types.NullType;
import ast.types.Type;

public class Register implements Source {
    private Type type;
    private String value;
    private Boolean isGlobal;

    private static int regCount = 0;

    public static void resetCount() {
        regCount = 0;
    }

    public Register(Type type, String val, Boolean isGlobal) {
        this.type = type;
        this.value = val;
        this.isGlobal = isGlobal;
    }

    //generic local register constructor
    public Register() {
        this.type = new NullType();
        this.value = Integer.toString(regCount++);
        this.isGlobal = false;
    }

    public Register(Type type) {
        this.type = type;
        this.value = Integer.toString(regCount++);
        this.isGlobal = false;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String getValue() {
        if (isGlobal) {
            return "@"+value;
        } else {
            return "%"+value;
        }
    }

    @Override
    public String getTypeString() {
        return TypeMap.ttos(type);
    }

    public Boolean getGlobal() {
        return isGlobal;
    }
}
