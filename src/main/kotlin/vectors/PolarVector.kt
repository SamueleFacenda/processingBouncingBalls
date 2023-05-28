package vectors

class PolarVector(
    private val length: Double,
    private val angle: Double
): Vector {

    override fun add(v: Vector): Vector {
        val other = convertToPolar(v)
        val newLengt = Math.sqrt(
            length * length +
            other.length * other.length +
            2 * length * other.length * Math.cos(angle - other.angle)
        )
        val newAngle = angle + Math.atan2(
            other.length * Math.sin(other.angle - angle),
            length + other.length * Math.cos(other.angle - angle)
        )
        return PolarVector(newLengt, newAngle)
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
        val x = length * Math.cos(angle)
        val y = length * Math.sin(angle)
        return CartesianVector(x, y)
    }

    private fun convertToPolar(v: Vector): PolarVector {
        return when (v) {
            is PolarVector -> v
            is CartesianVector -> v.toPolarVector()
            else -> throw IllegalArgumentException("Unknown vector type")
        }
    }
}