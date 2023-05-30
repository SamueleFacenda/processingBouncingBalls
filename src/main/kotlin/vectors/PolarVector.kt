package vectors

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PolarVector(
    private val length: Double,
    private val angle: Double
): Vector {

    override fun getX(): Double {
        return length * cos(angle)
    }

    override fun getY(): Double {
        return length * sin(angle)
    }

    override fun getLength(): Double {
        return length
    }

    override fun add(v: Vector): Vector {
        val other = convertToPolar(v)
        val newLength = sqrt(
            length * length +
            other.length * other.length +
            2 * length * other.length * cos(angle - other.angle)
        )
        val newAngle = angle + atan2(
            other.length * sin(other.angle - angle),
            length + other.length * cos(other.angle - angle)
        )
        return PolarVector(newLength, newAngle)
    }

    override fun subtract(v: Vector): Vector {
        val other = convertToPolar(v)
        val opposite = PolarVector(other.length, other.angle + Math.PI)
        return add(opposite)
    }

    override fun multiply(s: Double): Vector {
        return PolarVector(length * s, angle)
    }

    override fun divide(s: Double): Vector {
        return PolarVector(length / s, angle)
    }

    fun toCartesianVector(): CartesianVector {
        val x = length * cos(angle)
        val y = length * sin(angle)
        return CartesianVector(x, y)
    }

    private fun convertToPolar(v: Vector): PolarVector {
        return when (v) {
            is PolarVector -> v
            is CartesianVector -> v.toPolarVector()
            else -> throw IllegalArgumentException("Unknown vector type")
        }
    }

    override fun projectOnDirection(dir: Double): Vector {
        val newAngle = angle - dir
        return PolarVector(length * cos(newAngle), newAngle)
    }
}