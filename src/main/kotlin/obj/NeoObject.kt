package obj

import java.math.BigDecimal
import java.math.BigInteger

data class NeoObject(
    val type: Type,
    val value: Any?
) {
    enum class Type {
        Null,
        Bool,
        Int,
        Float,
        String,
        Function,
        Class,
    }

    constructor(int: Int) : this(
        Type.Int,
        BigInteger.valueOf(int.toLong())
    )

    constructor(long: Long) : this(
        Type.Int,
        BigInteger.valueOf(long)
    )

    constructor(double: Double) : this(
        Type.Float,
        double
    )

    constructor(bigInt: BigInteger) : this(
        Type.Int,
        bigInt
    )

    constructor(bigDec: BigDecimal) : this(
        Type.Float,
        bigDec
    )

    constructor(string: String) : this(
        Type.String,
        string
    )

    constructor(bool: Boolean) : this(
        Type.Bool,
        bool
    )

    constructor(function: Function) : this(
        Type.Function,
        function
    )

    constructor() : this(
        Type.Null,
        null
    )
}