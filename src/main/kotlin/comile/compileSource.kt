package comile

import ast.AstBuilder
import ast.Stmt
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import parser.MyParserLexer
import parser.MyParserParser

fun compileSource(source: String): Stmt.Program {
    // ✅ 3. 使用 CharStreams.fromString 创建输入流（输入是 String，已正确解码）
    val charStream = CharStreams.fromString(source)

    // 创建词法分析器
    val lexer = MyParserLexer(charStream)

    // 创建 token 流
    val tokens = CommonTokenStream(lexer)

    // 创建语法分析器
    val parser = MyParserParser(tokens)

    // 开始解析（例如从 program 开始）
    val cst: ParseTree = parser.program() // 明确类型

    // 创建 AST 构建器
    val astBuilder = AstBuilder()

    // 访问 CST 构建 AST
    val ast = astBuilder.visit(cst)

    return ast as Stmt.Program
}