package ast;

import ast.statements.Statement;
import ast.types.FunctionType;
import ast.types.PointerType;
import ast.types.Type;
import ast.types.VoidType;
import instructions.*;

import java.util.*;
import java.util.stream.Collectors;

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
    public static PhiInstruction returnPhi;


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
            AllocaInstruction returnAlloca = new AllocaInstruction(returnReg);
            prologue.addCode(returnAlloca);
        }

        //declare a label for returns to jump to
        returnLabel = new Label("returnLabel");

        for (Declaration decl: allDecls) {
            Register localVar = Register.genTypedLocalRegister(new PointerType(decl.getType().copy()), prologue.getLabel());
            AllocaInstruction alloca = new AllocaInstruction(localVar);
            prologue.addCode(alloca);
            func.addLocalBinding(decl.getName(), localVar);
        }

        for (int i = 0; i < params.size(); i++) {
            Declaration param = params.get(i);
            Register implicitReg = implicitRegs.get(i);
            Register localVar = func.lookupReg(param.getName());
            StoreInstruction store = new StoreInstruction(localVar, implicitReg);
            prologue.addCode(store);
        }


        BasicBlock endOfBody = body.toStackBlocks(func.getBody(), func);

        // if the last statement does not end with a call to return, and a branch to the return statement
        if (!endOfBody.endsWithJump()) {
            UnconditionalBranchInstruction returnBridge = new UnconditionalBranchInstruction(Function.returnLabel);
            endOfBody.addCode(returnBridge);
        }

        BasicBlock epilogue = new BasicBlock();
        endOfBody.addChild(epilogue);
        func.addToQueue(epilogue);

        //add the return jump label
        epilogue.setLabel(returnLabel);

        if (retType instanceof VoidType) {
            ReturnVoidInstruction retVoid = new ReturnVoidInstruction();
            epilogue.addCode(retVoid);
        } else {
            Register loadResult = Register.genTypedLocalRegister(retType.copy(), epilogue.getLabel());
            LoadInstruction load = new LoadInstruction(loadResult, returnReg);
            ReturnInstruction ret = new ReturnInstruction(loadResult);

            epilogue.addCode(load);
            epilogue.addCode(ret);
        }

        return func;
    }


    public IrFunction toSSACFG(IrProgram prog) {
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
            returnPhi = new PhiInstruction(null, null, new ArrayList<>());
        }

        //declare a label for returns to jump to
        returnLabel = new Label("returnLabel");

        BasicBlock endOfBody = body.toSSABlocks(func.getBody(), func);

        // if the last statement does not end with a call to return, and a branch to the return statement
        if (!endOfBody.endsWithJump()) {
            UnconditionalBranchInstruction returnBridge = new UnconditionalBranchInstruction(Function.returnLabel);
            endOfBody.addCode(returnBridge);
        }

        BasicBlock epilogue = new BasicBlock();
        endOfBody.addChild(epilogue);
        func.addToQueue(epilogue);

        //add the return jump label
        epilogue.setLabel(returnLabel);

        if (retType instanceof VoidType) {
            ReturnVoidInstruction retVoid = new ReturnVoidInstruction();
            epilogue.addCode(retVoid);
        } else {
            returnReg = Register.genTypedLocalRegister(retType.copy(), prologue.getLabel());
            returnPhi.setResult(returnReg);
            returnPhi.setBoundName(returnReg.getName());
            ReturnInstruction ret = new ReturnInstruction(returnReg);
            epilogue.addCode(returnPhi);
            epilogue.addCode(ret);
        }

        removeRedundantPhis(func);
        bubblePhisToTop(func);

        return func;
    }

    public void removeRedundantPhis(IrFunction func) {
        boolean check = true;

        // clean up redundant phis
        while(check) {
            check = false;
            for (BasicBlock block : func.getPreorderQueue()) {
                Queue<Instruction> code = block.getContents();
                int size = code.size();
                for (int i = 0; i < size; i++) {
                    Instruction inst = code.poll();
                    if (!(inst instanceof PhiInstruction)) {
                        code.add(inst);
                        continue;
                    }

                    PhiInstruction phi = (PhiInstruction) inst;
                    if (!(phi.isRedundant())) {
                        code.add(inst);
                        continue;
                    }

                    check = true;
                    substAll(phi.getResult(), phi.getMembers().get(0), func);
                }
            }
        }
    }

    public void substAll(Register phiResult, Source replacement, IrFunction func) {
        for (BasicBlock block : func.getPreorderQueue()) {
            Queue<Instruction> code = block.getContents();
            for (int i = 0; i < code.size(); i++) {
                Instruction inst = code.poll();
                inst.substitute(phiResult.copy(), replacement.copy());
                code.add(inst);
            }
        }
    }

    public void bubblePhisToTop(IrFunction func) {
        for (BasicBlock block : func.getPreorderQueue()) {
            Queue<Instruction> code = block.getContents();
            Queue<Instruction> buffer = new ArrayDeque<>();
            int size = code.size();
            for (int i = 0; i < size; i++) {
                Instruction inst = code.poll();
                if (inst instanceof PhiInstruction) {
                    code.add(inst);
                } else {
                    buffer.add(inst);
                }
            }
            while(!buffer.isEmpty()) {
                code.add(buffer.poll());
            }
        }
    }

}
