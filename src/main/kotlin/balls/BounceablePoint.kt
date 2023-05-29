package balls

import vectors.CartesianVector
import vectors.PolarVector
import vectors.Vector
import kotlin.reflect.KProperty

class BounceablePoint(
    val mass: Double = 0.0,
    val speed: Vector = CartesianVector(0.0, 0.0),
    val x: Double = 0.0,
    val y: Double = 0.0
) {

    fun moveOfTime(time: Double): BounceablePoint {
        val newX = x + speed.getX() * time
        val newY = y + speed.getY() * time
        return BounceablePoint(mass, speed, newX, newY)
    }

    fun reverseX(): BounceablePoint {
        val newSpeed = CartesianVector(-speed.getX(), speed.getY())
        return BounceablePoint(mass, newSpeed, x, y)
    }

    fun reverseY(): BounceablePoint {
        val newSpeed = CartesianVector(speed.getX(), -speed.getY())
        return BounceablePoint(mass, newSpeed, x, y)
    }

    fun accelerate(v: Vector): BounceablePoint {
        val newSpeed = speed.add(v)
        return BounceablePoint(mass, newSpeed, x, y)
    }

    operator fun plus(v: Vector): BounceablePoint {
        return accelerate(v)
    }

    fun divideSpeed(s: Double): BounceablePoint {
        val newSpeed = speed / s
        return BounceablePoint(mass, newSpeed, x, y)
    }

    operator fun div(s: Double): BounceablePoint {
        return divideSpeed(s)
    }

    fun multiplySpeed(s: Double): BounceablePoint {
        val newSpeed = speed * s
        return BounceablePoint(mass, newSpeed, x, y)
    }

    operator fun times(s: Double): BounceablePoint {
        return multiplySpeed(s)
    }

    fun withSpeed(v: Vector): BounceablePoint {
        return BounceablePoint(mass, v, x, y)
    }

    fun bounceOn(other: BounceablePoint): BounceablePoint {

        // m1 * v1 * cos B1 + m2 * v2 * cos B2 = m1 * u1 * cos A1 + m2 * u2 * cos A2
        // v1 * sin B1 = u1 * sin A1
        // v2 * sin B2 = u2 * sin A2
        // v2 * cos B2 - v1 * cos B1 = u1 * cos A1 - u2 * cos A2

        val angleOfCentersLine = Math.atan2(other.y - y, other.x - x)
        val Alpha1 = angleOfCentersLine - Math.atan2(speed.getY(), speed.getX())
        val Alpha2 = angleOfCentersLine - Math.atan2(other.speed.getY(), other.speed.getX())
        val u1 = speed.getLength()
        val u2 = other.speed.getLength()

        // v1 = (u1 * sin A1) / sin B1
        // v2 = (u2 * sin A2) / sin B2

        // u2 * sin A2 / sin B2 * cos B2 - u1 * sin A1 / sin B1 * cos B1 = u1 * cos A1 - u2 * cos A2
        // u2 * sin A2 * cos B2 / sin B2 - u1 * sin A1 * cos B1 / sin B1 = val1
        val val1 = u1 * Math.cos(Alpha1) - u2 * Math.cos(Alpha2)

        // u2 * sin A2 * cos B2 / sin B2 =  u1 * sin A1 * cos B1 / sin B1  + val1
        // cos B2 / sin B2 = (u1 * sin A1 * cos B1 / sin B1  + val1) / (u2 * sin A2)
        // cos B2 = (u1 * sin A1 * cos B1 / sin B1 / u2 * sin A2 + val1 / u2 / sin A2) * sin B2
        // cos B2 / sin B2 = val2 * cos B1 / sin B1 + val3
        val val2 = u1 * Math.sin(Alpha1) / u2 / Math.sin(Alpha2)
        val val3 = val1 / u2 / Math.sin(Alpha2)

        // (sin B2 / cos B2) ^ -1 = val2 * (sin B1 / cos B1) ^ -1 + val3
        // 1 / tan B2 = val2 /tan B1 + val3
        // tan B2 = 1 / (val2 /tan B1 + val3)
        // B2 = atan(1 / (val2 /tan B1 + val3))

        // m1 * v1 * cos B1 + m2 * v2 * cos B2 = m1 * u1 * cos A1 + m2 * u2 * cos A2
        // m1 * (u1 * sin A1) / sin B1 * cos B1 + m2 * (u2 * sin A2) / sin B2 * cos B2 = m1 * u1 * cos A1 + m2 * u2 * cos A2
        // m1 * (u1 * sin A1) / sin B1 * cos B1 + m2 * (u2 * sin A2) * (val2 * cos B1 / sin B1 + val3) = m1 * u1 * cos A1 + m2 * u2 * cos A2
        // (cos B1 / sin B1) ( m1 * u1 * sin A1 + m2 * u2 * sin A2 * val2) + m2 * u2 * sin A2 * val3 = m1 * u1 * cos A1 + m2 * u2 * cos A2
        // (cos B1 / sin B1) ( m1 * u1 * sin A1 + m2 * u2 * sin A2 * val2) = m1 * u1 * cos A1 + m2 * u2 * cos A2 - m2 * u2 * sin A2 * val3
        // (cos B1 / sin B1) = (m1 * u1 * cos A1 + m2 * u2 * cos A2 - m2 * u2 * sin A2 * val3) / ( m1 * u1 * sin A1 + m2 * u2 * sin A2 * val2)
        // sin B1 / cos B1 = ( m1 * u1 * sin A1 + m2 * u2 * sin A2 * val2) / (m1 * u1 * cos A1 + m2 * u2 * cos A2 - m2 * u2 * sin A2 * val3)
        // B1 = atan(( m1 * u1 * sin A1 + m2 * u2 * sin A2 * val2) / (m1 * u1 * cos A1 + m2 * u2 * cos A2 - m2 * u2 * sin A2 * val3))

        val B1 = Math.atan2(
            mass * u1 * Math.sin(Alpha1) + other.mass * u2 * Math.sin(Alpha2) * val2,
            mass * u1 * Math.cos(Alpha1) + other.mass * u2 * Math.cos(Alpha2) - other.mass * u2 * Math.sin(Alpha2) * val3
        )
        /*val B2 = Math.atan2(
            1.0,
            val2 / Math.tan(B1) + val3
        )*/
        val v1 = (u1 * Math.sin(Alpha1)) / Math.sin(B1)
        //val v2 = (u2 * Math.sin(Alpha2)) / Math.sin(B2)

        val newSpeed = PolarVector(v1, angleOfCentersLine - B1)
        return BounceablePoint(mass, newSpeed, x, y)
    }

    operator fun getValue(ball: Ball, property: KProperty<*>): Double {
        when (property.name) {
            "x" -> return x
            "y" -> return y
            else -> throw IllegalArgumentException("Unknown property ${property.name}")
        }
    }

}