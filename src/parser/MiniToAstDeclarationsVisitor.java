package parser;

import ast.declarations.Declaration;
import ast.types.Type;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.List;
import java.util.ArrayList;

public class MiniToAstDeclarationsVisitor
   extends MiniBaseVisitor<List<Declaration>>
{
   private final MiniToAstTypeVisitor typeVisitor = new MiniToAstTypeVisitor();

   @Override
   public List<Declaration> visitDeclarations(
      MiniParser.DeclarationsContext ctx)
   {
      List<Declaration> decls = new ArrayList<>();

      for (MiniParser.DeclarationContext dctx : ctx.declaration())
      {
         addDeclarationsTo(dctx, decls);
      }

      return decls;
   }

   private void addDeclarationsTo(MiniParser.DeclarationContext ctx,
      List<Declaration> decls)
   {
      Type type = typeVisitor.visit(ctx.type());

      for (TerminalNode node : ctx.ID())
      {
         decls.add(new Declaration(node.getSymbol().getLine(), type,
            node.getText()));
      }
   }

   @Override
   protected List<Declaration> defaultResult()
   {
      return new ArrayList<>();
   }
}
