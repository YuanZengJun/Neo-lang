package ast

sealed interface Stmt : Node {
    data class Program(
        val statements: List<Stmt>,
        val hadError: Boolean
    ) : Stmt

    data class ExprStmt(
        val expr: Expr
    ) : Stmt

    data class Block(
        val statements: List<Stmt>
    ) : Stmt

    data class FunDecl(
        val name: Expr.Identifier,
        val arguments: List<Expr>,
        val body: Block
    ) : Stmt

    data class VarDecl(
        val name: Expr.Identifier,
        val init: Expr
    ) : Stmt

    data class IfStmt(
        val condition: Expr,
        val then: Stmt,
        val branch: Stmt?
    ) : Stmt

    data class PrintStmt(
        val value: Expr
    ) : Stmt

    data class ForStmt(
        val initializer: Stmt?,
        val condition: Expr?,
        val update: Expr?,
        val body: Stmt
    ) : Stmt

    data class WhileStmt(
        val condition: Expr,
        val body: Stmt
    ) : Stmt

    object Nop : Stmt {
        override fun toString(): String {
            return "NOP"
        }
    }
}