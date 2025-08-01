package ast

import obj.NeoObject
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.*
import parser.MyParserBaseVisitor
import parser.MyParserParser.*
import java.math.BigInteger

class AstBuilder : MyParserBaseVisitor<Any>() {
    var hadError = false
    // ================== 表达式相关 ==================

    override fun visitAddSubExpr(ctx: AddSubExprContext): Expr {
        val left = visit(ctx.expr(0)) as Expr
        val right = visit(ctx.expr(1)) as Expr
        val op = ctx.getChild(1).text
        return Expr.BinaryExpr(left, right, op)
    }

    override fun visitMulDivExpr(ctx: MulDivExprContext): Expr {
        val left = visit(ctx.expr(0)) as Expr
        val right = visit(ctx.expr(1)) as Expr
        val op = ctx.getChild(1).text
        return Expr.BinaryExpr(left, right, op)
    }

    override fun visitPrimaryExpr(ctx: PrimaryExprContext): Expr {
        val child = ctx.getChild(0)

        return when {
            child is TerminalNode && child.symbol.type == INT -> Expr.LiteralExpr(NeoObject(BigInteger(child.text)))
            child is TerminalNode && child.symbol.type == ID -> Expr.VariableExpr(Expr.Identifier(child.text))
            child is TerminalNode && child.symbol.type == FLOAT -> Expr.LiteralExpr(NeoObject(child.text.toDouble()))
            child is TerminalNode && child.symbol.type == TRUE -> Expr.LiteralExpr(NeoObject(true))
            child is TerminalNode && child.symbol.type == FALSE -> Expr.LiteralExpr(NeoObject(false))
            child is TerminalNode && child.symbol.type == STRING -> Expr.LiteralExpr(NeoObject(child.text.substring(1,child.text.length - 1)))
            else -> throw RuntimeException("Unknown primary expression: ${child.text}")
        }
    }

    // ================== 语句相关 ==================

    override fun visitExprStmt(ctx: ExprStmtContext): Stmt {
        val expr = visit(ctx.expr()) as Expr
        return Stmt.ExprStmt(expr)
    }

    override fun visitProgram(ctx: ProgramContext): Stmt {
        val stmts: List<Stmt> = ctx.stmt().map { visit(it) as Stmt }
        return Stmt.Program(stmts,hadError)
    }

    // ================== 其他访问方法 ==================

    override fun visit(tree: ParseTree?): Any? {
        return super.visit(tree)
    }

    override fun visitErrorNode(node: ErrorNode): Node? {
        hadError = true
        println("Syntax error at: ${node.text}")
        return Stmt.Nop
    }

    // ================== 可选扩展 ==================

    override fun visitAssignExpr(ctx: AssignExprContext): Expr {
        val varName = ctx.ID().text
        val value = visit(ctx.expr()) as Expr
        // 可扩展为 AssignExpr
        return Expr.AssignExpr(Expr.Identifier(varName), value) // 示例简化处理
    }

    override fun visitGroupExpr(ctx: GroupExprContext): Expr? {
        return visit(ctx.expr()) as? Expr
    }

    override fun visitChildren(node: RuleNode): Any? {
        return super.visitChildren(node)
    }

    override fun visitBlock(ctx: BlockContext): Node? {
        val statements = ctx.stmt()?.map {
            visit(it) as Stmt
        } ?: emptyList()

        return Stmt.Block(
            statements
        )
    }

    override fun visitCallExpr(ctx: CallExprContext): Any? {
        val function = visit(ctx.expr(0)) as Expr

        val arguments = if (ctx.expr(1) == null) {
            emptyList()
        } else {
            ctx.expr().subList(1,ctx.expr().size).map {
                visit(it) as Expr
            }
        }

        return Expr.CallExpr(
            function,
            arguments
        )
    }

    override fun visitBlockStmt(ctx: BlockStmtContext): Stmt {
        val statements = ctx.block().stmt().map {
            visit(it) as Stmt
        }

        return Stmt.Block(statements)
    }

    override fun visitIfStmt(ctx: IfStmtContext): Any? {
        val condition = visit(ctx.expr()) as Expr
        val then = visit(ctx.block()) as Stmt
        val branch = ctx.stmt()?.let {
            visit(it) as Stmt
        }

        return Stmt.IfStmt(
            condition,
            then,
            branch
        )
    }

    override fun visitPrimary(ctx: PrimaryContext): Any? {
        return visit(ctx.primaryExpr())
    }

    override fun visitPrintStmt(ctx: PrintStmtContext): Any? {
        return Stmt.PrintStmt(
            visit(ctx.expr()) as Expr
        )
    }

    override fun visitVarDecl(ctx: VarDeclContext): Any? {
        val name = visit(ctx.ID()) as Expr as Expr.Identifier
        val init = visit(ctx.expr()) as Expr

        return Stmt.VarDecl(
            name,
            init
        )
    }

    override fun visitArgumentList(ctx: ArgumentListContext): Any? {
        val arguments = mutableListOf<Expr.ArgumentExpr>()

        for ((id, type) in ctx.ID().zip(ctx.type())) {
            arguments.add(
                Expr.ArgumentExpr(
                    visit(id) as Expr,
                    visit(type) as Expr
                )
            )
        }

        return arguments
    }

    override fun visitFunDecl(ctx: FunDeclContext): Any? {
        val name = visit(ctx.ID()) as? Expr.Identifier

        if (name == null) {
            error(ctx, "No name after 'fun'.")
            return Stmt.Nop
        }

        val arguments = visit(ctx.argumentList()) as List<Expr>

        if (arguments.size > 255) {
            throw RuntimeException()
        }

        val body = visit(ctx.block()) as Stmt.Block

        return Stmt.FunDecl(
            name,
            arguments,
            body
        )
    }


    override fun visitComparsion(ctx: ComparsionContext): Any? {
        val op = ctx.getChild(1).text
        val left = visit(ctx.expr(0)) as Expr
        val right = visit(ctx.expr(1)) as Expr

        return Expr.BinaryExpr(
            left,
            right,
            op!!
        )
    }

    override fun visitEquality(ctx: EqualityContext): Any? {
        val op = ctx.getChild(1).text
        val left = visit(ctx.expr(0)) as Expr
        val right = visit(ctx.expr(1)) as Expr

        return Expr.BinaryExpr(
            left,
            right,
            op!!
        )
    }

    override fun visitForInit(ctx: ForInitContext?): Any? {
        return visit(ctx) as Stmt
    }

    override fun visitForUpdate(ctx: ForUpdateContext?): Any? {
        return visit(ctx) as Expr
    }

    override fun visitForLoop(ctx: ForLoopContext): Stmt {
        val init = ctx.forInit()?.let {
            visit(it) as Stmt
        }
        val condition = ctx.expr()?.let {
            visit(it) as Expr
        } ?: Expr.LiteralExpr(NeoObject(true))
        val update = ctx.forUpdate()?.let {
            visit(it) as Expr
        }
        val body = ctx.block()?.let {
            visit(it) as Stmt
        }
        if (body == null) {
            error(ctx, "No body")
            return Stmt.Nop
        }

        return Stmt.ForStmt(
            init,
            condition,
            update,
            body
        )
    }

    override fun visitWhileLoop(ctx: WhileLoopContext): Any? {
        val condition = visit(ctx.expr()) as Expr
        val body = visit(ctx.block()) as Stmt

        return Stmt.WhileStmt(
            condition,
            body
        )
    }

    private fun error(ctx: ParserRuleContext, message: String) {
        hadError = true
        println("\u001b[31m" + "line ${ctx.start.line}: Error at '${ctx.text}': $message" + "\u001b[0m")
    }

    override fun visitDotAccess(ctx: DotAccessContext): Any? {
        val selected = visit(ctx.expr()) as Expr
        val name = visit(ctx.ID()) as Expr.Identifier

        return Expr.SelectExpr(
            selected,
            name
        )
    }

    override fun visitSimpleType(ctx: SimpleTypeContext): Any? {
        return visit(ctx.ID()) as Expr
    }

    override fun visitTypeDotAccess(ctx: TypeDotAccessContext): Any? {
        val selectedExpr = visit(ctx.type()) as Expr
        val name = visit(ctx.ID()) as Expr.Identifier

        return Expr.SelectExpr(
            selectedExpr,
            name
        )
    }

    override fun visitUnaryMinus(ctx: UnaryMinusContext): Any? {
        val op = ctx.MINUS().text
        val expr = visit(ctx.expr()) as Expr

        return Expr.UnaryExpr(
            op,
            expr
        )
    }

    override fun visitUnaryPlus(ctx: UnaryPlusContext): Any? {
        val op = ctx.PLUS().text
        val expr = visit(ctx.expr()) as Expr

        return Expr.UnaryExpr(
            op,
            expr
        )
    }

    override fun visitTerminal(node: TerminalNode): Any? {
        return Expr.Identifier(node.text)
    }

    override fun visitEmptyStmt(ctx: EmptyStmtContext?): Any? {
        return Stmt.Nop
    }
}