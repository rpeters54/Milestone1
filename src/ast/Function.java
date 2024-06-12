package ast;

import ast.declarations.Declaration;
import ast.statements.Statement;
import ast.types.*;
import instructions.*;
import instructions.llvm.*;

import java.util.*;

public class Function implements Typed {
    private final int lineNum;
    private final String name;
    private final Type retType;
    private final List<Declaration> params;
    private final List<Declaration> locals;
    private final Statement body;

    public Function(int lineNum, String name, List<Declaration> params,
                    Type retType, List<Declaration> locals, Statement body) {
        this.lineNum = lineNum;
        this.name = name;
        this.params = params;
        this.retType = retType;
        this.locals = locals;
        this.body = body;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getName() {
        return name;
    }

    public List<Declaration> getParams() {
        return params;
    }

    public Type getRetType() {
        return retType;
    }

    public Type getType() {
        return new FunctionType(this);
    }

    public Statement getBody() {
        return body;
    }

    public List<Declaration> concatDecls() {
        List<Declaration> allDecls = new ArrayList<>(params);
        allDecls.addAll(locals);
        return allDecls;
    }

    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        /* add all defined locals and params to the type environment */
        List<Declaration> allDecls = concatDecls();
        try {
            env.batchExtend(allDecls);
        } catch (TypeException e) {
            throw new TypeException(String.format("Function: Failed To Extend " +
                    "Environment, line: %d", lineNum));
        }

        /* type check each statement in the function */
        Type type = body.typecheck(env);

        /* remove items added to the type environment */
        env.batchRemove(allDecls.size());

        /* return the type of the last evaluated statement */
        return type;
    }


    public static Label returnLabel;
    public static Register returnReg;
    public static PhiLLVMInstruction returnPhi;
    public static BasicBlock returnBlock;


    /**
     * Generates a CFG for the function in STACK form
     * No optimizations, (it's going to be slow)
     * @param prog The program object being updated
     * @return A object containing the CFGs in LLVM and ARM as well as some metadata
     */
    public IrFunction toStackCFG(IrProgram prog) {
        IrFunction func = new IrFunction(prog, this);
        BasicBlock prologue = func.getBody();
        prologue.setLabel(new Label("prologue"));

        // reset the register and label count back to zero
        Register.resetCount();
        Label.resetLabelCount();

        List<Declaration> allDecls = new ArrayList<>(params);
        allDecls.addAll(locals);

        // collect all implicitly defined regs
        List<Register> implicitRegs = new ArrayList<>();
        for (Declaration param: params) {
            implicitRegs.add(Register.genTypedLocalRegister(param.getType().copy(), prologue.getLabel()));
        }


        // if non-void return, declare a container to hold the return value (helps with cleanup)
        if (!(retType instanceof VoidType)) {
            returnReg = Register.genTypedLocalRegister(new PointerType(retType.copy()), prologue.getLabel());
            AllocaLLVMInstruction returnAlloca = new AllocaLLVMInstruction(returnReg);
            prologue.addCode(returnAlloca);
        }

        //declare a block and label for returns to jump to
        returnBlock = new BasicBlock();
        returnLabel = new Label("returnLabel");
        returnBlock.setLabel(returnLabel);


        for (Declaration decl: allDecls) {
            Register localVar = Register.genTypedLocalRegister(new PointerType(decl.getType().copy()), prologue.getLabel());
            AllocaLLVMInstruction alloca = new AllocaLLVMInstruction(localVar);
            prologue.addCode(alloca);
            func.addLocalBinding(decl.getName(), localVar);
        }

        for (int i = 0; i < params.size(); i++) {
            Declaration param = params.get(i);
            Register implicitReg = implicitRegs.get(i);
            Register localVar = func.lookupReg(param.getName());
            StoreLLVMInstruction store = new StoreLLVMInstruction(implicitReg, localVar);
            prologue.addCode(store);
        }


        BasicBlock endOfBody = body.toStackBlocks(func.getBody(), func);


        // if the last statement does not end with a call to return, and a branch to the return statement
        if (!endOfBody.endsWithJump()) {
            UnconditionalBranchLLVMInstruction returnBridge = new UnconditionalBranchLLVMInstruction(Function.returnLabel);
            endOfBody.addCode(returnBridge);
            endOfBody.addChild(returnBlock);
        }

        func.addToQueue(returnBlock);

        if (retType instanceof VoidType) {
            ReturnVoidLLVMInstruction retVoid = new ReturnVoidLLVMInstruction();
            returnBlock.addCode(retVoid);
        } else {
            Register loadResult = Register.genTypedLocalRegister(retType.copy(), returnBlock.getLabel());
            LoadLLVMInstruction load = new LoadLLVMInstruction(loadResult, returnReg);
            ReturnLLVMInstruction ret = new ReturnLLVMInstruction(retType,loadResult);

            returnBlock.addCode(load);
            returnBlock.addCode(ret);
        }

        func.toArm();
        func.registerAllocation();

        return func;
    }


    /**
     * Generates a CFG for the function in SSA form
     * Optimizations are enabled by default
     * @param prog The program object being updated
     * @return A object containing the CFGs in LLVM and ARM as well as some other metadata
     */
    public IrFunction toSSACFG(IrProgram prog, boolean transformations) {
        IrFunction func = new IrFunction(prog, this);
        BasicBlock prologue = func.getBody();
        prologue.setLabel(new Label("prologue"));

        // reset the register and label count back to zero
        Register.resetCount();
        Label.resetLabelCount();

        // define all parameters in the prologue env
        for (Declaration param: params) {
            prologue.addLocalBinding(param.getName(),
                    Register.genTypedLocalRegister(param.getType().copy(), prologue.getLabel()));
        }

        // if non-void return, declare a container to hold the return value (helps with cleanup)
        if (!(retType instanceof VoidType)) {
            returnPhi = new PhiLLVMInstruction(null, null, new ArrayList<>());
        }

        //declare a block and label for returns to jump to
        returnBlock = new BasicBlock();
        returnLabel = new Label("returnLabel");
        returnBlock.setLabel(returnLabel);

        BasicBlock endOfBody = body.toSSABlocks(func.getBody(), func);

        // if the last statement does not end with a call to return, add a branch to the return statement
        if (!endOfBody.endsWithJump()) {
            UnconditionalBranchLLVMInstruction returnBridge = new UnconditionalBranchLLVMInstruction(Function.returnLabel);
            endOfBody.addCode(returnBridge);
            endOfBody.addChild(returnBlock);
        }

        func.addToQueue(returnBlock);


        if (retType instanceof VoidType) {
            ReturnVoidLLVMInstruction retVoid = new ReturnVoidLLVMInstruction();
            returnBlock.addCode(retVoid);
        } else {
            returnReg = Register.genTypedLocalRegister(retType.copy(), prologue.getLabel());
            returnPhi.setResult(returnReg);
            returnPhi.setBoundName(returnReg.toString());
            ReturnLLVMInstruction ret = new ReturnLLVMInstruction(retType, returnReg);
            // add return phi register -> instruction binding
            returnBlock.addCode(returnPhi);
            returnBlock.addCode(ret);
        }


        // do basic transformations on the completed program
        // (aggressive dead code elim / constant prop and fold)
        func.phiCleanup();
        if (transformations)
            func.runTransformations();
        func.toArm();
        func.registerAllocation();

        return func;
    }

}
