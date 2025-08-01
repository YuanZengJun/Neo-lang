package vm

import obj.NeoObject
import obj.Function

class VirtualMachine(
    val maxStackCount: Int = 2048,
) {
    // 1. 新增：全局变量存储区（存放全局变量和全局函数）
    private val globalVariables = mutableListOf<NeoObject?>(null)  // 索引从0开始，动态扩容

    // 栈帧栈
    private val frameStack: ArrayDeque<Frame> = ArrayDeque()

    // 操作数栈（每个帧独立，通过currentFrame访问）
    private var isRunning = false

    // 获取当前栈帧（栈顶）
    private val currentFrame: Frame
        get() = frameStack.last()

    private val operandStack: ArrayDeque<NeoObject>
        get() = currentFrame.operandStack

    private var pc: Int
        get() = currentFrame.pc
        set(value) {
            currentFrame.pc = value
        }

    private val locals: Array<NeoObject?>
        get() = currentFrame.locals

    private val chunk: Chunk
        get() = currentFrame.chunk

    private val chunkCode: ArrayDeque<Byte>
        get() = chunk.getCode()

    // 2. 执行入口：接收全局chunk，启动执行（包含调用main的指令）
    fun executeGlobal(chunk: Chunk) {
        // 初始化全局帧（全局chunk的执行上下文）
        frameStack.addLast(Frame(chunk, 0))
        isRunning = true

        // 执行全局chunk的指令（包括LOAD_GLOBAL main + CALL 0）
        while (isRunning && frameStack.isNotEmpty()) {
            val opcode = fetchOpcode()
            // 判断是否需要跳过当前循环（移除帧并继续）
            if (opcode == null) {
                frameStack.removeLast()
                continue  // 直接在 while 循环内使用 continue，无 lambda 嵌套
            }
            // 处理指令
            handleOpcode(opcode)
        }
    }

    // 统一处理指令
    private fun handleOpcode(opcode: Opcode) {
        when (opcode) {
            Opcode.NOP -> {}
            Opcode.LOAD_CONST -> executeLoadConst()
            Opcode.LOAD_CONST_LONG -> executeLoadConstLong()
            // 3. 新增：全局变量存储指令
            Opcode.STORE_GLOBAL -> executeStoreGlobal()
            Opcode.STORE_GLOBAL_LONG -> executeStoreGlobalLong()
            // 4. 新增：全局变量加载指令（关键：加载main函数）
            Opcode.LOAD_GLOBAL -> executeLoadGlobal()
            Opcode.LOAD_GLOBAL_LONG -> executeLoadGlobalLong()
            // 局部变量指令（保持不变）
            Opcode.STORE_VAR -> executeStoreVar()
            Opcode.STORE_VAR_LONG -> executeStoreVarLong()
            Opcode.LOAD_VAR -> executeLoadVar()
            Opcode.LOAD_VAR_LONG -> executeLoadVarLong()
            // 运算指令（保持不变）
            Opcode.ADD, Opcode.SUB, Opcode.MUL, Opcode.DIV, Opcode.Equal,
            Opcode.NotEqual, Opcode.GreaterEqual, Opcode.GreaterThan,
            Opcode.LessThan, Opcode.LessEqual -> executeBinaryOp(opcode)
            // 其他指令（保持不变）
            Opcode.PRINT -> executePrint()
            Opcode.POP -> popOperand()
            Opcode.NEGATE -> executeUnaryOp(opcode)
            Opcode.RETURN -> executeReturn()
            Opcode.CALL -> executeCall()  // 关键：调用main函数
            else -> throw UnknownError("未知指令: $opcode")
        }
    }

    // 5. 实现：加载全局变量（包括main函数）
    private fun executeLoadGlobal() {
        val index = readUShort().toInt()
        // 全局变量区动态扩容（确保索引有效）
        while (globalVariables.size <= index) {
            globalVariables.add(null)
        }
        val value = globalVariables[index] ?: throw RuntimeException("全局变量未初始化: index=$index")
        pushOperand(value)
    }

    // 6. 实现：加载大索引全局变量
    private fun executeLoadGlobalLong() {
        val index = readUInt().toInt()
        while (globalVariables.size <= index) {
            globalVariables.add(null)
        }
        val value = globalVariables[index] ?: throw RuntimeException("全局变量未初始化: index=$index")
        pushOperand(value)
    }

    // 7. 实现：存储全局变量（用于初始化全局函数和全局变量）
    private fun executeStoreGlobal() {
        val index = readUShort().toInt()
        val value = popOperand()
        while (globalVariables.size <= index) {
            globalVariables.add(null)
        }
        globalVariables[index] = value
    }

    // 8. 实现：存储大索引全局变量
    private fun executeStoreGlobalLong() {
        val index = readUInt().toInt()
        val value = popOperand()
        while (globalVariables.size <= index) {
            globalVariables.add(null)
        }
        globalVariables[index] = value
    }

    // 函数调用逻辑（确保main函数能被正确调用）
    private fun executeCall() {
        // 读取参数数量（1字节）
        val actualArgCount = chunkCode[pc++].toUByte().toInt()

        // 弹出参数（从操作数栈）
        val args = mutableListOf<NeoObject>()
        repeat(actualArgCount) {
            if (operandStack.isEmpty()) {
                throw RuntimeException("参数数量不足，预期$actualArgCount")
            }
            args.add(0, operandStack.removeLast())  // 保持参数顺序
        }

        // 弹出函数对象（main函数作为全局函数存储在全局变量区，此处已通过LOAD_GLOBAL加载）
        val funcObj = operandStack.removeLast()
        check(funcObj.type == NeoObject.Type.Function) { "调用的不是函数: ${funcObj.type}" }
        val function = funcObj.value as Function

        // 校验参数数量（main函数通常无参数，actualArgCount应为0）
        check(actualArgCount == function.paramCount) {
            "参数不匹配: 预期${function.paramCount}，实际$actualArgCount"
        }

        // 创建新栈帧（执行main函数体）
        val newFrame = Frame(
            chunk = function.chunk,
            returnAddress = pc  // 记录返回后继续执行的位置（全局chunk后续指令）
        )

        // 初始化函数参数（main函数无参数时此处为空）
        args.forEachIndexed { i, arg ->
            if (i < newFrame.locals.size) {
                newFrame.locals[i] = arg
            }
        }

        // 切换到main函数的栈帧
        frameStack.addLast(newFrame)
    }

    // 函数返回逻辑（main函数执行完毕后结束程序）
    private fun executeReturn() {
        val currentFrame = frameStack.removeLast()
        // 若函数无返回值，默认压入空值
        val returnValue = currentFrame.operandStack.lastOrNull() ?: NeoObject()

        if (frameStack.isNotEmpty()) {
            val callerFrame = frameStack.last()
            callerFrame.pc = currentFrame.returnAddress
            callerFrame.operandStack.addLast(returnValue)  // 确保返回值入栈
        } else {
            isRunning = false
        }
    }

    // 以下为原有方法（保持不变，仅补充必要注释）
    private fun executeLoadVar() {
        val varIndex = readUShort().toInt()
        val value = locals[varIndex] ?: throw RuntimeException("局部变量未初始化: $varIndex")
        pushOperand(value)
    }

    private fun executeLoadVarLong() {
        val varIndex = readUInt().toInt()
        val value = locals[varIndex] ?: throw RuntimeException("局部变量未初始化: $varIndex")
        pushOperand(value)
    }

    private fun executeStoreVar() {
        val varIndex = readUShort().toInt()
        val value = popOperand()
        locals[varIndex] = value
    }

    private fun executeStoreVarLong() {
        val varIndex = readUInt().toInt()
        val value = popOperand()
        locals[varIndex] = value
    }

    private fun executePrint() {
        val value = popOperand()
        println(value.value)
    }

    private fun executeBinaryOp(op: Opcode) {
        val right = popOperand()
        val left = popOperand()
        val resultType = UniversalTable.getResultType(left.type, right.type)
        val convertedLeft = UniversalTable.convertToType(left, left.type, resultType)
        val convertedRight = UniversalTable.convertToType(right, right.type, resultType)
        val handler = UniversalTable.getHandler(op, resultType)
        pushOperand(handler(convertedLeft, convertedRight))
    }

    private fun executeUnaryOp(op: Opcode) {
        val operand = popOperand()
        val handler = when (op) {
            Opcode.NEGATE -> UniversalTable.getUnaryNegHandler(operand.type)
            else -> throw IllegalArgumentException("未知一元运算符: $op")
        }
        pushOperand(handler(operand))
    }

    private fun executeLoadConst() {
        val index = readUShort().toInt()
        val constant = chunk.getConstant(index.toUInt())
        pushOperand(constant)
    }

    private fun executeLoadConstLong() {
        val index = readUInt()
        val constant = chunk.getConstant(index)
        pushOperand(constant)
    }

    private fun readUInt(): UInt {
        if (pc + 3 >= chunkCode.size) {
            throw RuntimeException("字节码不完整，无法读取UInt")
        }
        val b1 = chunkCode[pc].toUInt() and 0xFFu
        val b2 = chunkCode[pc + 1].toUInt() and 0xFFu
        val b3 = chunkCode[pc + 2].toUInt() and 0xFFu
        val b4 = chunkCode[pc + 3].toUInt() and 0xFFu
        pc += 4
        return (b1 shl 24) or (b2 shl 16) or (b3 shl 8) or b4
    }

    fun readUShort(): UShort {
        if (pc + 1 >= chunkCode.size) {
            throw RuntimeException("字节码不完整，无法读取UShort")
        }
        val highByte = chunkCode[pc].toInt() and 0xFF
        val lowByte = chunkCode[pc + 1].toInt() and 0xFF
        pc += 2
        return ((highByte shl 8) or lowByte).toUShort()
    }

    private fun fetchOpcode(): Opcode? {
        if (pc >= chunkCode.size) return null
        val opcodeByte = chunkCode[pc]
        pc++
        return Opcode.fromValue(opcodeByte)
    }

    private fun pushOperand(value: NeoObject) = operandStack.addLast(value)
    private fun popOperand(): NeoObject = operandStack.removeLast()
}