package vm

import obj.NeoObject
import obj.NeoObject.Type
import java.math.BigInteger
import kotlin.math.floor

typealias BinaryOpHandler = (NeoObject, NeoObject) -> NeoObject

object UniversalTable {
    private val typePromotion: Map<Pair<Type, Type>, Type> = mapOf(
        (Type.Int to Type.Int) to Type.Int,
        (Type.Int to Type.Float) to Type.Float,
        (Type.Float to Type.Int) to Type.Float,
        (Type.Float to Type.Float) to Type.Float,
    )

    private fun <T> buildOpHandlers(
        opcode: Opcode,
        intOp: (BigInteger, BigInteger) -> T,
        floatOp: (Double, Double) -> T,
        intResultType: (T) -> NeoObject = {
            when (it) {
                is BigInteger -> NeoObject(it )
                is Double -> NeoObject(it)
                else -> NeoObject()
            }
        },
        floatResultType: (T) -> NeoObject = {
            when (it) {
                is BigInteger -> NeoObject(it )
                is Double -> NeoObject(it)
                else -> NeoObject()
            }
        },
        overrideIntOp: ((NeoObject, NeoObject) -> NeoObject)? = null
    ): Map<Pair<Opcode, Type>, BinaryOpHandler> {
        return mapOf(
            // 整数+整数
            (opcode to Type.Int) to (overrideIntOp ?: { a, b ->
                val aInt = a.value as BigInteger
                val bInt = b.value as BigInteger
                intResultType(intOp(aInt, bInt))
            }),
            // 浮点数+整数（自动提升为浮点数）
            (opcode to Type.Float) to { a, b ->
                val aFloat = a.value as Double
                val bFloat = b.value as Double
                floatResultType(floatOp(aFloat, bFloat))
            }
        )
    }

    private fun buildOpHandlers(
        opcode: Opcode,
        intOp: (BigInteger, BigInteger) -> Boolean,  // 整数比较逻辑
        floatOp: (Double, Double) -> Boolean,        // 浮点数比较逻辑
        resultType: (Boolean) -> NeoObject = { NeoObject(it) } // 结果包装
    ): Map<Pair<Opcode, Type>, BinaryOpHandler> {
        return mapOf(
            (opcode to Type.Int) to { a, b ->
                val aInt = a.value as BigInteger
                val bInt = b.value as BigInteger
                resultType(intOp(aInt, bInt))
            },
            (opcode to Type.Float) to { a, b ->
                val aFloat = a.value as Double
                val bFloat = b.value as Double
                resultType(floatOp(aFloat, bFloat))
            }
        )
    }

    val opHandlers = buildOpHandlers(Opcode.ADD, BigInteger::plus, Double::plus) +
            buildOpHandlers(Opcode.SUB, BigInteger::minus, Double::minus) +
            buildOpHandlers(Opcode.MUL, BigInteger::times, Double::times) +
            buildOpHandlers(
                Opcode.DIV,
                intOp = { a, b -> a.toDouble() / b.toDouble() },
                floatOp = { a, b -> a / b },
                intResultType = { NeoObject(it) }
            ) +
            buildOpHandlers(
                Opcode.GreaterThan,
                intOp = { a, b -> a > b },
                floatOp = { a, b -> a > b }
            ) +
            buildOpHandlers(
                Opcode.LessThan,
                intOp = { a, b -> a < b },
                floatOp = { a, b -> a < b }
            ) +
            buildOpHandlers(
                Opcode.Equal,
                intOp = { a, b -> a == b },
                floatOp = { a, b -> a == b }
            ) +
            buildOpHandlers(
                Opcode.GreaterEqual,
                intOp = { a, b -> a >= b },
                floatOp = { a, b -> a >= b }
            ) +
            buildOpHandlers(
                Opcode.LessEqual,
                intOp = { a, b -> a <= b },
                floatOp = { a, b -> a <= b }
            ) +
            buildOpHandlers(
                Opcode.NotEqual,
                intOp = { a, b -> a != b },
                floatOp = { a, b -> a != b }
            )



    fun getResultType(leftType: Type, rightType: Type): Type {
        return typePromotion[leftType to rightType]
            ?: throw IllegalArgumentException("不支持的类型组合: $leftType + $rightType")
    }

    fun getHandler(op: Opcode, resultType: Type): BinaryOpHandler {
        return opHandlers[op to resultType]
            ?: throw IllegalArgumentException("不支持的操作: $op($resultType)")
    }

    fun convertToType(value: NeoObject, fromType: Type, toType: Type): NeoObject {
        val convertedValue = when (fromType to toType) {
            Type.Int to Type.Float -> (value.value as BigInteger).toDouble()
            Type.Float to Type.Int -> (value.value as Double).toLong().toBigInteger()
            Type.Int to Type.String -> (value.value as BigInteger).toString()
            Type.Float to Type.String -> (value.value as Double).toString()
            else -> value.value  // 类型相同无需转换
        }

        return NeoObject(type=toType,value=convertedValue)
    }

    fun getUnaryNegHandler(type: Type): (NeoObject) -> NeoObject {
        return when (type) {
            Type.Int -> { obj -> NeoObject(Type.Int,-(obj.value as BigInteger)) }
            Type.Float -> { obj -> NeoObject(Type.Float, -(obj.value as Double)) }
            else -> throw RuntimeException("不支持的一元取反类型: $type")
        }
    }

    // 逻辑非（NOT）的处理器：处理布尔和数值类型
    fun getUnaryNotHandler(type: Type): (NeoObject) -> NeoObject {
        return when (type) {
            Type.Bool -> { obj -> NeoObject(Type.Bool,!(obj.value as Boolean)) }
            Type.Int -> { obj -> NeoObject(Type.Bool,(obj.value as Int) == 0) }  // 0为true，非0为false
            Type.Float -> { obj -> NeoObject( Type.Bool, (obj.value as Double) == 0.0) }
            else -> throw RuntimeException("不支持的逻辑非类型: $type")
        }
    }
}