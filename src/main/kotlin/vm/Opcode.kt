package vm

enum class Opcode {
    NOP,
    POP,
    LOAD_CONST,
    LOAD_CONST_LONG,
    STORE_VAR,
    STORE_VAR_LONG,
    STORE_GLOBAL,
    STORE_GLOBAL_LONG,
    LOAD_VAR,
    LOAD_VAR_LONG,
    LOAD_GLOBAL,
    LOAD_GLOBAL_LONG,
    CALL,
    GET_PROPERTY,
    PRINT,
    ADD,
    SUB,
    MUL,
    DIV,
    NEGATE,
    GreaterThan,  // 大于 (>)
    LessThan,  // 小于 (<)
    Equal,  // 等于 (==)
    GreaterEqual,  // 大于等于 (>=)
    LessEqual,  // 小于等于 (<=)
    NotEqual,   // 不等于 (!=)
    RETURN;

    val value = ordinal.toByte()

    companion object {
        fun fromValue(value: Byte): Opcode? {
            val index = value.toInt()
            return if (index >= 0 && index < Opcode.entries.size) {
                Opcode.entries[index]
            } else {
                null
            }
        }
    }
}