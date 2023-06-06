package balls

import vectors.CartesianVector
import vectors.PolarVector
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.reflect.KProperty

private const val CHECK_FUTURE_COLLISIONS = true

open class BounceableBall(
    protected var physicReference: BounceablePoint,
    private val depth: Int = 0,
    val innerRadius: Double,
    val outerRadius: Double,
) {

    val x: Double by this
    val y: Double by this

    operator fun getValue(ball: BounceableBall, property: KProperty<*>): Double {
        return when (property.name) {
            "x" -> physicReference.x
            "y" -> physicReference.y
            else -> throw IllegalArgumentException("Unknown property ${property.name}")
        }
    }

    fun needToBounceWith(other: BounceableBall): Boolean{
        return hasCollisionWith(other) && isConvergentWith(other)
    }

    private fun hasCollisionWith(other: BounceableBall): Boolean{
        return if (CHECK_FUTURE_COLLISIONS){
            willCollideWith(other)
        } else {
            isCollidingWith(other)
        }
    }

    private fun willCollideWith(other: BounceableBall): Boolean {
        return when {
            depth == other.depth -> willCollideWithBorderOF(other)
            depth > other.depth -> willCollideWithInsideOf(other)
            else -> other.willCollideWithInsideOf(this)
        }
    }

    private fun willCollideWithBorderOF(other: BounceableBall): Boolean {
        val thisNextPosition = physicReference.moveOfTime(1.0 / 60)
        val otherNextPosition = other.physicReference.moveOfTime(1.0 / 60)
        return sqrt(
            (thisNextPosition.x - otherNextPosition.x).pow(2.0) +
                    (thisNextPosition.y - otherNextPosition.y).pow(2.0)
        ) < outerRadius + other.outerRadius
    }

    private fun willCollideWithInsideOf(other: BounceableBall): Boolean {
        val thisNextPosition = physicReference.moveOfTime(1.0 / 60.0)
        val otherNextPosition = other.physicReference.moveOfTime(1.0 / 60.0)
        return sqrt(
            (thisNextPosition.x - otherNextPosition.x).pow(2.0) +
                    (thisNextPosition.y - otherNextPosition.y).pow(2.0)
        ) +
                outerRadius > other.innerRadius
    }

    private fun isCollidingWith(other: BounceableBall): Boolean{
        return when {
            depth == other.depth -> isCollidingWithBorderOf(other)
            depth > other.depth -> isCollidingWithTheInsideOf(other)
            else -> other.isCollidingWithTheInsideOf(this)
        }
    }

    fun isCollidingWithTheInsideOf(other: BounceableBall): Boolean{
        if (depth != other.depth + 1){
            throw IllegalArgumentException("Cannot check inner collision between balls of depth $depth and ${other.depth}")
        }

        return sqrt((other.x - x).pow(2.0) + (other.y - y).pow(2.0)) + outerRadius > other.innerRadius
    }

    private fun isCollidingWithBorderOf(other: BounceableBall): Boolean{
        if (depth != other.depth){
            throw IllegalArgumentException("Cannot check outer collision between balls of depth $depth and ${other.depth}")
        }

        return sqrt((other.x - x).pow(2.0) + (other.y - y).pow(2.0)) < outerRadius + other.outerRadius
    }

    private fun isConvergentWith(other: BounceableBall): Boolean{
        return when {
            depth == other.depth -> isConvergentWithCenterOf(other)
            depth > other.depth -> isConvergentWithInternalBorderOf(other)
            else -> other.isConvergentWithInternalBorderOf(this)
        }
    }

    private fun isConvergentWithCenterOf(other: BounceableBall): Boolean {
        return physicReference.isConvergentOn(other.physicReference)
    }

    private fun isConvergentWithInternalBorderOf(other: BounceableBall): Boolean {
        val otherDirection = atan2(y - other.y, x - other.x)
        val otherReference = other.physicReference.moveOf(PolarVector(other.innerRadius, otherDirection))
        return physicReference.isConvergentOn(otherReference)
    }

    fun checkForBounceOnBoundaryOf(width: Double, height: Double) {
        checkUpperBoundary()
        checkLowerBoundary(height)
        checkLeftBoundary()
        checkRightBoundary(width)
    }

    private fun checkUpperBoundary() {
        if (y - outerRadius < 0) {
            if (physicReference.speed.getY() < 0.0) {
                physicReference = physicReference.reverseY()
            } else {
                physicReference += CartesianVector(0.0, 0.1)
            }
        }
    }

    private fun checkLowerBoundary(height: Double) {
        if (y + outerRadius > height) {
            if (physicReference.speed.getY() > 0.0) {
                physicReference = physicReference.reverseY()
            } else {
                physicReference += CartesianVector(0.0, -0.1)
            }
        }
    }

    private fun checkLeftBoundary() {
        if (x - outerRadius < 0) {
            if (physicReference.speed.getX() < 0.0) {
                physicReference = physicReference.reverseX()
            } else {
                physicReference += CartesianVector(0.1, 0.0)
            }
        }
    }

    private fun checkRightBoundary(width: Double) {
        if (x + outerRadius > width) {
            if (physicReference.speed.getX() > 0.0) {
                physicReference = physicReference.reverseX()
            } else {
                physicReference += CartesianVector(-0.1, 0.0)
            }
        }
    }

    override fun toString(): String {
        return "Ball3(depth=$depth) ${hashCode()}"
    }
}