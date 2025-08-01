package comile

import ast.Expr
import ast.Stmt
import obj.Function
import obj.NeoObject
import vm.Chunk
import vm.Opcode

class CompileAst {
    private var globalVarCount = 0

    private val scopeStack = ArrayDeque<MutableMap<Expr.Identifier, Int>>()
    private val currentScope: MutableMap<Expr.Identifier, Int>
        get() = scopeStack.last()

    init {
        scopeStack.addFirst(mutableMapOf<Expr.Identifier, Int>())
    }

    fun compile(program: Stmt.Program): Chunk {
        val globalChunk: Chunk = Chunk()

        for (stmt in program.statements) {
            compileStmt(globalChunk,stmt)
        }

        val mainId = Expr.Identifier("main")
        val mainIndex = currentScope[mainId]
            ?: throw RuntimeException("没有找到main函数")

        if (mainIndex <= UShort.MAX_VALUE.toInt()) {
            globalChunk.writeOpcode(Opcode.LOAD_CONST)
            globalChunk.writeUShort(mainIndex.toUShort())
        } else {
            globalChunk.writeOpcode(Opcode.LOAD_CONST_LONG)
            globalChunk.writeUInt(mainIndex.toUInt())
        }
        globalChunk.writeOpcode(Opcode.CALL)
        globalChunk.write(0.toByte())

        return globalChunk
    }

    fun compileStmt(chunk: Chunk,stmt: Stmt) {
        when (stmt) {
            is Stmt.ExprStmt -> {
                compileExpr(chunk,stmt.expr)

                chunk.writeOpcode(Opcode.POP)
            }
            is Stmt.PrintStmt -> {
                compileExpr(chunk,stmt.value)

                chunk.writeOpcode(Opcode.PRINT)
            }
            is Stmt.Nop -> {
                chunk.writeOpcode(Opcode.NOP)
            }
            is Stmt.FunDecl -> {
                // 1. 编译函数体到独立的Chunk
                val funChunk = Chunk()
                val localScope = mutableMapOf<Expr.Identifier, Int>()
                scopeStack.addFirst(localScope)  // 函数内部作用域入栈

                // 2. 处理函数参数（作为局部变量存入函数作用域）
                stmt.arguments.forEachIndexed { i, param ->
                    if (param is Expr.Identifier) {
                        localScope[param] = i  // 参数索引从0开始
                        funChunk.localCount++  // 局部变量计数包含参数
                    }
                }

                // 3. 编译函数体语句（支持访问全局变量）
                for (statement in stmt.body.statements) {
                    compileStmt(funChunk, statement)  // 函数体内可通过scopeStack查找全局变量
                }

                // 4. 确保函数有返回指令
                if (funChunk.getCode().isEmpty() || funChunk.getCode().last() != Opcode.RETURN.value) {
                    funChunk.writeOpcode(Opcode.RETURN)
                }

                // 5. 创建函数对象
                val function = Function(
                    funChunk,
                    stmt.arguments.size,
                    funChunk.localCount
                )

                // 6. 恢复作用域（弹出函数内部作用域）
                scopeStack.removeFirst()

                // 7. 区分全局函数和局部函数的存储逻辑
                val isGlobalFunction = scopeStack.size == 1  // 全局作用域只有1层（顶层）
                val varIndex: Int

                if (isGlobalFunction) {
                    // 7.1 全局函数：存入全局变量区，使用全局指令
                    // 将函数对象添加到常量池并加载
                    val funcConstIndex = chunk.addConstant(NeoObject(function))
                    chunk.writeOpcode(Opcode.LOAD_CONST)
                    chunk.writeUShort(funcConstIndex.toUShort())

                    // 分配全局索引并存储
                    varIndex = globalVarCount  // 使用全局变量计数器
                    if (varIndex <= UShort.MAX_VALUE.toInt()) {
                        chunk.writeOpcode(Opcode.STORE_GLOBAL)  // 全局存储指令
                        chunk.writeUShort(varIndex.toUShort())
                    } else {
                        chunk.writeOpcode(Opcode.STORE_GLOBAL_LONG)
                        chunk.writeUInt(varIndex.toUInt())
                    }
                    globalVarCount++  // 全局计数器自增
                } else {
                    // 7.2 局部函数：存入当前作用域的局部变量区
                    // 加载函数对象（通过writeLoadConstAuto添加到常量池并加载）
                    chunk.writeLoadConstAuto(NeoObject(function))

                    // 分配局部索引并存储
                    varIndex = chunk.localCount
                    if (varIndex <= UShort.MAX_VALUE.toInt()) {
                        chunk.writeOpcode(Opcode.STORE_VAR)  // 局部存储指令
                        chunk.writeUShort(varIndex.toUShort())
                    } else {
                        chunk.writeOpcode(Opcode.STORE_VAR_LONG)
                        chunk.writeUInt(varIndex.toUInt())
                    }
                    chunk.localCount++  // 局部计数器自增
                }

                // 8. 将函数名注册到当前作用域（全局/局部）
                currentScope[stmt.name] = varIndex
            }
            is Stmt.VarDecl -> {
                // 1. 编译初始化表达式（如 let a = 1 + 2 中的 1 + 2）
                compileExpr(chunk, stmt.init)

                // 2. 判断当前作用域类型（全局/局部）
                val isGlobalScope = scopeStack.size == 1  // 全局作用域只有1层（初始化时的顶层作用域）
                val varIndex: Int

                // 3. 分配变量索引并生成存储指令
                if (isGlobalScope) {
                    // 全局变量：使用独立的全局计数器，生成全局存储指令
                    varIndex = globalVarCount  // 全局变量索引从0开始递增
                    if (varIndex <= UShort.MAX_VALUE.toInt()) {
                        chunk.writeOpcode(Opcode.STORE_GLOBAL)  // 全局变量专用指令
                        chunk.writeUShort(varIndex.toUShort())
                    } else {
                        chunk.writeOpcode(Opcode.STORE_GLOBAL_LONG)
                        chunk.writeUInt(varIndex.toUInt())
                    }
                    globalVarCount++  // 全局计数器自增
                } else {
                    // 局部变量：使用当前chunk的localCount，生成局部存储指令
                    varIndex = chunk.localCount  // 局部变量索引从0开始（包含函数参数）
                    if (varIndex <= UShort.MAX_VALUE.toInt()) {
                        chunk.writeOpcode(Opcode.STORE_VAR)  // 局部变量专用指令
                        chunk.writeUShort(varIndex.toUShort())
                    } else {
                        chunk.writeOpcode(Opcode.STORE_VAR_LONG)
                        chunk.writeUInt(varIndex.toUInt())
                    }
                    chunk.localCount++  // 局部计数器自增
                }

                // 4. 将变量名与索引注册到当前作用域（供后续访问）
                currentScope[stmt.name] = varIndex
            }
            else -> throw RuntimeException("Unknow Stmt $stmt")
        }
    }

    // ... 其他原有方法（compile、compileFunctionDef等）保持不变 ...

    private fun compileExpr(chunk: Chunk,expr: Expr) {
        when (expr) {
            // 2. 处理函数调用（callee可以是Identifier或Select）
            is Expr.CallExpr -> {
                // 步骤1：编译被调用者（callee）
                // 无论是Identifier还是Select，编译后栈顶都会是函数对象
                compileExpr(chunk,expr.function)

                // 步骤2：编译所有参数并依次压栈
                expr.arguments.forEach { argExpr ->
                    compileExpr(chunk,argExpr)
                }

                // 步骤3：生成CALL指令，操作数为参数数量
                chunk.writeOpcode(Opcode.CALL)
                chunk.write(expr.arguments.size.toByte())
            }
            is Expr.LiteralExpr -> {
                chunk.writeLoadConstAuto(expr.value)
            }
            is Expr.BinaryExpr -> {
                compileExpr(chunk,expr.left)
                compileExpr(chunk,expr.right)

                val opcode = when (expr.operator) {
                    "+" -> Opcode.ADD
                    "-" -> Opcode.SUB
                    "*" -> Opcode.MUL
                    "/" -> Opcode.DIV
                    "==" -> Opcode.Equal
                    "!=" -> Opcode.NotEqual
                    ">" -> Opcode.GreaterThan
                    ">=" -> Opcode.GreaterEqual
                    "<" -> Opcode.LessThan
                    "<=" -> Opcode.LessEqual
                    else -> throw RuntimeException("Unknow Operator ${expr.operator}")
                }

                chunk.writeOpcode(opcode)
            }

            is Expr.UnaryExpr -> {
                compileExpr(chunk,expr.value)

                val opcode = when (expr.op) {
                    "-" -> Opcode.NEGATE
                    else -> throw RuntimeException("Unknow Operator ${expr.op}")
                }

                chunk.writeOpcode(opcode)
            }

            // 移除重复的Expr.Identifier分支，统一用VariableExpr处理所有变量访问
            is Expr.VariableExpr -> {
                var foundIndex: Int? = null
                var isGlobal = false

                // 遍历作用域栈（从当前作用域到全局作用域）
                for (scope in scopeStack.reversed()) {  // 去掉level，直接遍历作用域
                    if (scope.containsKey(expr.name)) {
                        foundIndex = scope[expr.name]
                        // 关键修正：全局作用域是栈底元素（scopeStack.last()）
                        isGlobal = (scope === scopeStack.last())
                        break
                    }
                }

                if (foundIndex == null) {
                    throw RuntimeException("未定义的变量 '${expr.name.name}'")
                }
                val index = foundIndex

                // 根据是否为全局变量生成对应指令
                if (isGlobal) {
                    // 全局变量：生成LOAD_GLOBAL
                    if (index <= UShort.MAX_VALUE.toInt()) {
                        chunk.writeOpcode(Opcode.LOAD_GLOBAL)
                        chunk.writeUShort(index.toUShort())
                    } else {
                        chunk.writeOpcode(Opcode.LOAD_GLOBAL_LONG)
                        chunk.writeUInt(index.toUInt())
                    }
                } else {
                    // 局部变量：生成LOAD_VAR
                    if (index <= UShort.MAX_VALUE.toInt()) {
                        chunk.writeOpcode(Opcode.LOAD_VAR)
                        chunk.writeUShort(index.toUShort())
                    } else {
                        chunk.writeOpcode(Opcode.LOAD_VAR_LONG)
                        chunk.writeUInt(index.toUInt())
                    }
                }
            }

            else -> throw RuntimeException("Unknow Expr $expr")
        }
    }
}