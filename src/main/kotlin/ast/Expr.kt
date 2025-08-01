package ast

import obj.NeoObject

sealed interface Expr : Node {
    data class BinaryExpr(
        val left: Expr,
        val right: Expr,
        val operator: String
    ) : Expr

    data class LiteralExpr(
        val value: NeoObject
    ) : Expr

    data class Identifier(
        val name: String
    ) : Expr

    data class VariableExpr(
        val name: Identifier
    ) : Expr

    data class CallExpr(
        val function: Expr,
        val arguments: List<Expr>
    ) : Expr

    data class ArgumentExpr(
        val name: Expr,
        val type: Expr
    ) : Expr

    data class AssignExpr(
        val identifier: Identifier,
        val value: Expr
    ) : Expr

    data class SelectExpr(
        val selected: Expr,
        val name: Identifier
    ) : Expr

    data class UnaryExpr(
        val op: String,
        val value: Expr
    ) : Expr
}