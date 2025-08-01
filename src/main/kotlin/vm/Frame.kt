package vm

import obj.NeoObject

data class Frame(
    val chunk: Chunk,
    val returnAddress: Int, // 返回地址(函数调用返回后跳转到的位置)
    val locals: Array<NeoObject?> = arrayOfNulls(chunk.localCount),
    val operandStack: ArrayDeque<NeoObject> = ArrayDeque(),
    var pc: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Frame

        if (returnAddress != other.returnAddress) return false
        if (pc != other.pc) return false
        if (chunk != other.chunk) return false
        if (!locals.contentEquals(other.locals)) return false
        if (operandStack != other.operandStack) return false

        return true
    }

    override fun hashCode(): Int {
        var result = returnAddress
        result = 31 * result + pc
        result = 31 * result + chunk.hashCode()
        result = 31 * result + locals.contentHashCode()
        result = 31 * result + operandStack.hashCode()
        return result
    }
}