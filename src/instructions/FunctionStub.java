package instructions;

import ast.types.FunctionType;
import ast.types.NullType;
import ast.types.Type;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionStub implements Source {
    private final FunctionType type;
    private final String name;

    public FunctionStub(FunctionType type, String name) {
        this.type = type;
        this.name = name;
    }


    public Type getType() {
        return type;
    }

    @Override
    public String getValue() {
        return "@"+name;
    }

    @Override
    public String getTypeString() {
        return TypeMap.ttos(type.getOutput());
    }

    public List<String> getArgTypeString() {
        return type.getInputs().stream().map(TypeMap::ttos).collect(Collectors.toList());
    }
}
