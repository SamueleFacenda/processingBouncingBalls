package vectors

import kotlin.math.atan2
import kotlin.math.sqrt


class CartesianVector(
    private val x: Double,
    private val y: Double
): Vector {

    override fun getX(): Double {
        return x
    }

    override fun getY(): Double {
        return y
    }

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

    override fun multiply(s: Double): Vector {
        return CartesianVector(x * s, y * s)
    }

    override fun divide(s: Double): Vector {
        return CartesianVector(x / s, y / s)
    }

    fun toPolarVector(): PolarVector {
        val length = sqrt(x * x + y * y)
        val angle = atan2(y, x)
        return PolarVector(length, angle)
    }

    private fun convertToCartesian(v: Vector): CartesianVector {
        return when (v) {
            is PolarVector -> v.toCartesianVector()
            is CartesianVector -> v
            else -> throw IllegalArgumentException("Unknown vector type")
        }
    }

    override fun getProjectOn(dir: Double): Vector {
        return toPolarVector().getProjectOn(dir)
    }
}