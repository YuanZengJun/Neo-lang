import comile.CompileAst
import comile.compileSource
import vm.VirtualMachine
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.system.measureTimeMillis

fun main() {
    // ✅ 1. 创建一个明确使用 UTF-8 编码的 BufferedReader
    val reader = BufferedReader(InputStreamReader(System.`in`, StandardCharsets.UTF_8))

    try {
        while (true) {
            print(">>> ")
            // ✅ 2. 使用我们创建的 reader，而不是 readLine()
            val input = reader.readLine() ?: break // 如果输入流关闭 (Ctrl+D/Ctrl+Z)，则退出循环

            // 检查退出命令
            val trimmedInput = input.trim().lowercase()
            if (trimmedInput == "exit" || trimmedInput == "quit" || trimmedInput == "bye") {
                println("再见！")
                break
            }

            // 测量解析和执行时间
            val durationMs = measureTimeMillis {
                try {
                    val ast = compileSource(trimmedInput)

                    val compiler = CompileAst()

                    val chunk = compiler.compile(ast)

                    //chunk.disassemble("反汇编")
                    //println()

                    val virtualMachine = VirtualMachine()

                    virtualMachine.executeGlobal(chunk)
                } catch (e: Exception) {
                    e.printStackTrace()
                    System.err.println("解析错误: ${e.message}")
                }
            }

            println("用时: ${durationMs / 1000.0} 秒")

        }
    } catch (e: Exception) {
        // ✅ 5. 捕获读取过程中的异常（虽然 BufferedReader 应该能处理 UTF-8）
        System.err.println("输入读取错误: ${e.message}")
    } finally {
        // ✅ 6. 确保资源被正确关闭
        reader.close()
    }
}