package vectors


class CartesianVector(
    private val x: Double,
    private val y: Double
): Vector {
    override fun add(v: Vector): Vector {
        val other = convertToCartesian(v)
        val newX = x + other.x
        val newY = y + other.y
        return CartesianVector(newX, newY)
    }

    override fun subtract(v: Vector): Vector {
        val other = convertToCartesian(v)
        val newX = x - other.x
        val newY = y - other.y
        return CartesianVector(newX, newY)
    }

    fun toPolarVector(): PolarVector {
        val length = Math.sqrt(x * x + y * y)
        val angle = Math.atan2(y, x)
        return PolarVector(length, angle)
    }

    private fun convertToCartesian(v: Vector): CartesianVector {
        return when (v) {
            is PolarVector -> v.toCartesianVector()
            is CartesianVector -> v
            else -> throw IllegalArgumentException("Unknown vector type")
        }
    }
}