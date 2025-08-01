// Generated from MyParser.g4 by ANTLR 4.13.1
package parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MyParserParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MyParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MyParserParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MyParserParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IfStmt}
	 * labeled alternative in {@link MyParserParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStmt(MyParserParser.IfStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PrintStmt}
	 * labeled alternative in {@link MyParserParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrintStmt(MyParserParser.PrintStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code VarDecl}
	 * labeled alternative in {@link MyParserParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDecl(MyParserParser.VarDeclContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FunDecl}
	 * labeled alternative in {@link MyParserParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunDecl(MyParserParser.FunDeclContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ForLoop}
	 * labeled alternative in {@link MyParserParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLoop(MyParserParser.ForLoopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code WhileLoop}
	 * labeled alternative in {@link MyParserParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileLoop(MyParserParser.WhileLoopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BlockStmt}
	 * labeled alternative in {@link MyParserParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStmt(MyParserParser.BlockStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExprStmt}
	 * labeled alternative in {@link MyParserParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprStmt(MyParserParser.ExprStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EmptyStmt}
	 * labeled alternative in {@link MyParserParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStmt(MyParserParser.EmptyStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MyParserParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(MyParserParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MyParserParser#terminator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerminator(MyParserParser.TerminatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link MyParserParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(MyParserParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MyParserParser#forInit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForInit(MyParserParser.ForInitContext ctx);
	/**
	 * Visit a parse tree produced by {@link MyParserParser#forUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForUpdate(MyParserParser.ForUpdateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SimpleType}
	 * labeled alternative in {@link MyParserParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleType(MyParserParser.SimpleTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TypeDotAccess}
	 * labeled alternative in {@link MyParserParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDotAccess(MyParserParser.TypeDotAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code GroupExpr}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupExpr(MyParserParser.GroupExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code DotAccess}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDotAccess(MyParserParser.DotAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MulDivExpr}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulDivExpr(MyParserParser.MulDivExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Comparsion}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparsion(MyParserParser.ComparsionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Primary}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(MyParserParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UnaryPlus}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryPlus(MyParserParser.UnaryPlusContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UnaryMinus}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryMinus(MyParserParser.UnaryMinusContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CallExpr}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallExpr(MyParserParser.CallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Equality}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEquality(MyParserParser.EqualityContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AddSubExpr}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddSubExpr(MyParserParser.AddSubExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AssignExpr}
	 * labeled alternative in {@link MyParserParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignExpr(MyParserParser.AssignExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MyParserParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpr(MyParserParser.PrimaryExprContext ctx);
}