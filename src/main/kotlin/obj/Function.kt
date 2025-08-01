package obj

import vm.Chunk

data class Function(
    val chunk: Chunk,          // 函数体字节码
    val paramCount: Int,       // 参数数量
    val localVarCount: Int     // 局部变量总数（含参数）
)