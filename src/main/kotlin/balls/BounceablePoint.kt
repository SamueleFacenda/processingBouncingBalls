package balls

import vectors.CartesianVector
import vectors.PolarVector
import vectors.Vector
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.reflect.KProperty

class BounceablePoint(
    private val mass: Double = 0.0,
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

    private fun accelerate(v: Vector): BounceablePoint {
        val newSpeed = speed + v
        return BounceablePoint(mass, newSpeed, x, y)
    }

    operator fun plus(v: Vector): BounceablePoint {
        return accelerate(v)
    }

    operator fun minus(v: Vector): BounceablePoint {
        return BounceablePoint(mass, speed - v, x, y)
    }

    private fun divideSpeed(s: Double): BounceablePoint {
        val newSpeed = speed / s
        return BounceablePoint(mass, newSpeed, x, y)
    }

    operator fun div(s: Double): BounceablePoint {
        return divideSpeed(s)
    }

    private fun multiplySpeed(s: Double): BounceablePoint {
        val newSpeed = speed * s
        return BounceablePoint(mass, newSpeed, x, y)
    }

    operator fun times(s: Double): BounceablePoint {
        return multiplySpeed(s)
    }

    fun withSpeed(v: Vector): BounceablePoint {
        return BounceablePoint(mass, v, x, y)
    }

    fun moveOf(v: Vector): BounceablePoint {
        val newX = x + v.getX()
        val newY = y + v.getY()
        return BounceablePoint(mass, speed, newX, newY)
    }

    fun bounceOn(other: BounceablePoint): BounceablePoint {
        val impactDirection = atan2(other.y - y, other.x - x)

        val thisSpeed = speed.getProjectOn(impactDirection).getLength()
        val otherSpeed = other.speed.getProjectOn(impactDirection).getLength()

        // formula: https://www.youmath.it/lezioni/fisica/dinamica/2999-urti-elastici.html
        val thisFinalSpeed = (thisSpeed * (mass - other.mass) + otherSpeed * (2 * other.mass)) / (mass + other.mass)

        return this + PolarVector(thisFinalSpeed, impactDirection) - PolarVector(thisSpeed, impactDirection)
    }

    fun areConvergent(other: BounceablePoint): Boolean {
        val impactDirection = atan2(other.y - y, other.x - x)
        val thisSpeed = speed.getProjectOn(impactDirection).getLength()
        val otherSpeed = other.speed.getProjectOn(impactDirection).getLength()
        return thisSpeed > otherSpeed
    }

    operator fun getValue(ball: Ball, property: KProperty<*>): Double {
        return when (property.name) {
            "x" -> x
            "y" -> y
            else -> throw IllegalArgumentException("Unknown property ${property.name}")
        }
    }

}