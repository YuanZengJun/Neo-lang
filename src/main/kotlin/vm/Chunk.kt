// vm/Chunk.kt
package vm

import obj.NeoObject
import java.util.List.copyOf

/**
 * 代表一段编译后的字节码。
 * 包含操作码序列和常量池。
 */
class Chunk(
    private val maxConstant: Int = Int.MAX_VALUE
) {
    // 存储原始字节码指令（操作码 + 操作数）
    private val code: ArrayDeque<Byte> = ArrayDeque()

    // 存储常量池（字面量：数字、字符串等）
    private val constants: MutableList<NeoObject> = mutableListOf()

    var localCount: Int = 0

    // --- 常量池操作 ---

    /**
     * 将一个常量添加到常量池。
     * @param value 要添加的常量值。
     * @return 该常量在常量池中的索引。
     */
    fun addConstant(value: NeoObject): UInt {
        require(constants.size + 1 <= maxConstant) {
            "Constants Overflow."
        }

        val existingIndex = constants.indexOf(value)
        if (existingIndex != -1) {
            return constants.indexOf(value).toUInt()
        }

        constants.add(value)
        return (constants.size - 1).toUInt()
    }

    /**
     * 根据索引获取常量池中的常量。
     * @param index 常量的索引。
     * @return 对应的常量值。
     * @throws IllegalArgumentException 如果索引无效。
     */
    fun getConstant(index: UInt): NeoObject {
        require(index >= 0u && index < constants.size.toUInt()) {
            "Constant index $index out of bounds [0, ${constants.size})"
        }
        require(index < Int.MAX_VALUE.toUInt()) {
            "Constant index $index out of ${Int.MAX_VALUE}"
        }

        return constants[index.toInt()]
    }

    // --- 字节码写入操作 ---

    /**
     * 向字节码流中写入一个字节。
     * @param byte 要写入的字节。
     */
    fun write(byte: Byte) {
        code.addLast(byte)
    }

    /**
     * 向字节码流中写入一个操作码。
     * @param opcode 要写入的操作码。
     */
    fun writeOpcode(opcode: Opcode) {
        write(opcode.value)
    }

    fun writeUInt(value: UInt) {
        code.add((value shr 24).toByte()) // 最高8位
        code.add((value shr 16).toByte())
        code.add((value shr 8).toByte())
        code.add(value.toByte())
    }

    /**
     * 写入一个短整型（2字节）到字节码流。
     * 使用大端序（高位字节在前）。
     * @param value 要写入的短整型值。
     */
    fun writeUShort(value: UShort) {
        val highByte = (value.toInt() shr 8 and 0xFF).toByte()  // 高位字节
        val lowByte = (value.toInt() and 0xFF).toByte()          // 低位字节

        code.addLast(highByte)
        code.addLast(lowByte)
    }

    fun writeLoadConstAuto(value: NeoObject) {
        val indexUInt = addConstant(value)

        if (indexUInt <= UShort.MAX_VALUE.toUInt()) {
            // 索引在2字节范围内：使用LOAD_CONST（短指令）
            writeOpcode(Opcode.LOAD_CONST)
            writeUShort(indexUInt.toUShort())
        } else {
            // 索引超过2字节：使用LOAD_CONST_LONG（长指令）
            writeOpcode(Opcode.LOAD_CONST_LONG)
            writeUInt(indexUInt)
        }
    }

    // ... 可以添加更多 writeXXX 方法，例如 writeInt, writeByte ...

    // --- 反汇编（调试用）---

    /**
     * 将字节码反汇编成人类可读的形式并打印。
     * @param name 可选的名称，用于标识这段代码。
     */
    fun disassemble(name: String = "") {
        if (name.isNotEmpty()) {
            println("== $name ==")
        }

        println("constants $constants")

        var offset = 0
        while (offset < code.size) {
            // 打印当前指令的偏移量
            print("%04d ".format(offset))

            // 读取操作码
            val opcodeValue = code[offset]
            val opcode = Opcode.fromValue(opcodeValue) ?: Opcode.NOP // 如果无效，用 NOP 代替

            when (opcode) {
                Opcode.NOP -> {
                    println("NOP")
                    offset += 1 // 消费1个字节
                }
                Opcode.LOAD_CONST -> {
                    // LOAD_CONST 后面跟着一个2字节的常量索引
                    if (offset + 3 > code.size) {
                        println("LOAD_CONST [ERROR: missing operand]")
                        offset = code.size // 跳出循环
                        break
                    }
                    // 读取接下来的两个字节作为索引
                    val highByte = code[offset + 1].toInt() and 0xFF
                    val lowByte = code[offset + 2].toInt() and 0xFF
                    val constIndex = (highByte shl 8) or lowByte

                    val constant = try {
                        getConstant(constIndex.toUInt())
                    } catch (e: IllegalArgumentException) {
                        "INVALID_CONSTANT($constIndex)"
                    }
                    println("LOAD_CONST $constIndex '$constant'")
                    offset += 3 // 消费操作码(1) + 索引(2) = 3 字节
                }
                Opcode.ADD -> {
                    println("ADD")
                    offset++
                }
                Opcode.SUB -> {
                    println("SUB")
                    offset++
                }
                Opcode.MUL -> {
                    println("MUL")
                    offset++
                }
                Opcode.DIV -> {
                    println("DIV")
                    offset++
                }
                Opcode.RETURN -> {
                    println("RETURN")
                    offset++
                }
                Opcode.PRINT -> {
                    println("PRINT")
                    offset++
                }
                Opcode.Equal -> {
                    println("Equal")
                    offset++
                }
                Opcode.NotEqual -> {
                    println("NotEqual")
                    offset++
                }
                Opcode.GreaterEqual -> {
                    println("GreaterEqual")
                    offset++
                }
                Opcode.LessThan -> {
                    println("LessThan")
                    offset++
                }
                Opcode.LessEqual -> {
                    println("LessEqual")
                    offset++
                }
                Opcode.GreaterThan -> {
                    println("GreaterThan")
                    offset++
                }
                Opcode.STORE_VAR -> {
                    val highByte = code[offset + 1].toInt() and 0xFF
                    val lowByte = code[offset + 2].toInt() and 0xFF
                    val constIndex = (highByte shl 8) or lowByte

                    println("STORE_VAR $constIndex")
                    offset+=3
                }

                Opcode.NEGATE -> {
                    println("NEGATE")
                    offset++
                }
                Opcode.POP -> {
                    println("POP")
                    offset++
                }
                Opcode.LOAD_VAR -> {
                    val highByte = code[offset + 1].toInt() and 0xFF
                    val lowByte = code[offset + 2].toInt() and 0xFF
                    val constIndex = (highByte shl 8) or lowByte

                    println("LOAD_VAR $constIndex '${constants[constIndex]}'")
                    offset += 3
                }
                else -> {
                    println("${opcode.name}")
                    offset += 1
                }
            }
        }
    }

    // --- 辅助方法 ---

    /**
     * 获取此 Chunk 的字节码副本。
     * @return 字节码的字节数组。
     */
    fun getCode(): ArrayDeque<Byte> = code

    fun copyOf(): ArrayDeque<Byte> {
        val copy = ArrayDeque<Byte>(code.size)
        System.arraycopy(code,0,copy,0,code.size)
        return copy
    }

    /**
     * 获取此 Chunk 的常量池副本。
     * @return 常量池列表。
     */
    fun getConstants(): List<Any?> = constants.toList()

    fun getOpcode(offset: Int): Opcode? {
        return if (offset >= 0 && offset < code.size) {
            Opcode.fromValue(code[offset])
        } else null
    }

    fun getUShortOperand(offset: Int): UShort? {
        if (offset + 2 > code.size) return null // 需要至少2个字节
        val highByte = code[offset].toInt() and 0xFF
        val lowByte = code[offset + 1].toInt() and 0xFF
        return ((highByte shl 8) or lowByte).toUShort()
    }

    override fun toString(): String {
        var string = String()

        string += "{code: $code,"
        string += "constant: $constants}"

        return string
    }
}