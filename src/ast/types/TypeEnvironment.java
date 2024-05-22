package ast.types;

import ast.declarations.Declaration;
import ast.declarations.TypeDeclaration;

import java.util.*;

public class TypeEnvironment {

    private static class Binding {
        private final String id;
        private final Type type;

        private Binding(String id, Type type) {
            this.id = id;
            this.type = type;
        }
    }

    private final LinkedList<Binding> bindings;
    private final ArrayList<TypeDeclaration> definedTypes;
    private FunctionType currentFunc;

    public TypeEnvironment() {
        bindings = new LinkedList<>();
        definedTypes = new ArrayList<>();
        currentFunc = null;
    }

    public ArrayList<TypeDeclaration> getDefinedTypes() {
        return definedTypes;
    }

    /* Manage list of declarations */

    public void extend(String id, Type type) throws TypeException {
        if (!isValid(type)) {
            throw new TypeException(String.format("extend: Undefined " +
                    "Type Binding %s Can't Be Added to Env", id));
        }
        bindings.addFirst(new Binding(id, type));
    }

    /* add some number of declarations to the type environment */
    public void batchExtend(List<Declaration> decls) throws TypeException {
        /* Check for duplicate Declarations */
        Set<Declaration> set = new HashSet<Declaration>(decls);
        if (decls.size() != set.size()) {
            throw new TypeException("batchExtend: Duplicate " +
                    "Type Binding %s Can't Be Added to Env");
        }
        /* Add all declarations to the type environment */
        for (Declaration decl : decls) {
            extend(decl.getName(), decl.getType());
        }
    }

    /* Remove "number" elements from the type environment */
    public void batchRemove(int number) {
        for (int i = 0; i < number; i++) {
            bindings.removeFirst();
        }
    }

    public Type lookup(String id) throws TypeException {
        for (Binding binding : bindings) {
            if (binding.id.equals(id)) {
                return binding.type;
            }
        }
        for (TypeDeclaration typeDecl : definedTypes) {
            if (typeDecl.getName().equals(id)) {
                return new StructType(typeDecl.getLineNum(), typeDecl.getName());
            }
        }
        throw new TypeException(String.format("lookup: Couldn't " +
                "Resolve Type of Identifier '%s'", id));
    }


    /* Manage defined types */

    /* Add type declaration to the environment */
    public boolean addTypeDeclaration(TypeDeclaration type) {
        /* if the type already exists, return false */
        for (TypeDeclaration typeDecl : definedTypes) {
            if (typeDecl.getName().equals(type.getName())) {
                return false;
            }
        }
        /* if any of the struct members aren't already defined, return false */
        definedTypes.add(type);
        for (Declaration field : type.getFields()) {
            Type fieldType = field.getType();
            if (!isValid(fieldType)) {
                definedTypes.remove(definedTypes.size()-1);
                return false;
            }
        }
        return true;
    }

    public TypeDeclaration getTypeDeclaration(String name) {
        for (TypeDeclaration typeDecl : definedTypes) {
            if (typeDecl.getName().equals(name)) {
                return typeDecl;
            }
        }
        return null;
    }

    /*
     * Returns true if the type is a valid instance of an already defined type
     */
    public boolean isValid(Type type) {
        StructType st;
        if (!(type instanceof StructType)) {
            return true;
        }

        st = (StructType) type;
        for (TypeDeclaration defined : definedTypes) {
            if (st.getName().equals(defined.getName())) {
                return true;
            }
        }
        return false;
    }

    /* Manage in-scope function */

    public FunctionType getCurrentFunc() {
        return currentFunc;
    }

    public void setCurrentFunc(FunctionType functionType) {
        currentFunc = functionType;
    }
}
