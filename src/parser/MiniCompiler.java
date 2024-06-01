package parser;

import ast.IrProgram;
import ast.Program;
import ast.types.TypeException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.util.Locale;

import static java.lang.System.exit;


public class MiniCompiler {
    public static void main(String[] args) throws IOException {
        ParseObject parseObj = parseParameters(args);
        CommonTokenStream tokens = new CommonTokenStream(createLexer(parseObj.infile));
        MiniParser parser = new MiniParser(tokens);
        ParseTree tree = parser.program();

        // return if there is a syntax error
        if (parser.getNumberOfSyntaxErrors() != 0) {
            error("Syntax Error");
        }

        // construct the AST
        MiniToAstProgramVisitor programVisitor = new MiniToAstProgramVisitor();
        ast.Program program = programVisitor.visit(tree);

        // check for main
        if (!program.validMain()) {
            error("Main Function Not Defined");
        }

        // type check
        try {
            program.validTypes();
        } catch (TypeException e) {
            e.printStackTrace();
            exit(1);
        }

        // validate returns
        if (!program.validReturns()) {
            error("Invalid Return Path");
        }

        IrProgram prog = program.toCFG(parseObj.cfgType);
        if (parseObj.dotfile != null) {
            prog.toDotFile(parseObj.dotfile);
        }
        if (parseObj.llfile != null) {
            prog.toLLFile(parseObj.llfile);
        }
        if (parseObj.armfile != null) {
            prog.toArmFile(parseObj.armfile);
        }
    }

    private static ParseObject parseParameters(String[] args) {
        if (args.length < 1)
            usage();

        ParseObject parser = new ParseObject();
        parser.infile = args[0];
        for (int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "-stack" -> parser.cfgType = Program.CFGType.STACK;
                case "-ssa" -> parser.cfgType = Program.CFGType.SSA;
                case "-dot" -> {
                    if (i+1 < args.length && !args[i+1].startsWith("-")) {
                        parser.dotfile = args[++i];
                    } else {
                        parser.dotfile = "a.dot";
                    }
                }
                case "-llvm" -> {
                    if (i+1 < args.length && !args[i+1].startsWith("-")) {
                        parser.llfile = args[++i];
                    } else {
                        parser.llfile = "a.ll";
                    }
                }
                case "-arm" -> {
                    if (i+1 < args.length && !args[i+1].startsWith("-")) {
                        parser.armfile = args[++i];
                    } else {
                        parser.armfile = "a.S";
                    }
                }
            }
        }
        return parser;
    }

    public static void error(String err) {
        System.err.println(err);
        exit(-1);
    }


    private static void usage() {
        System.err.println(
                "usage: <infile> [-s ssa/stack] [-d dotfile] [-l llfile]\n"
                +"Default: Produces SSA LLVM file 'out.ll'"
        );
        exit(-1);
    }

    private static MiniLexer createLexer(String infile) {
        try {
            CharStream input;
            if (infile == null) {
                input = CharStreams.fromStream(System.in);
            } else {
                input = CharStreams.fromFileName(infile);
            }
            return new MiniLexer(input);
        } catch (java.io.IOException e) {
            System.err.println("file not found: " + infile);
            exit(1);
            return null;
        }
    }

    private static class ParseObject {
        private String infile;
        private String llfile;
        private String dotfile;
        private String armfile;
        private Program.CFGType cfgType;

        private ParseObject() {
            infile = null;
            llfile = null;
            dotfile = null;
            armfile = null;
            cfgType = Program.CFGType.SSA;
        }
    }
}
