package balls

import processing.core.PApplet
import vectors.CartesianVector
import vectors.Vector
import kotlin.random.Random
import kotlin.reflect.KProperty

class Ball(
    private val numberOfChildren: Int = 0,
    private val depth: Int = 0,
    private val maxDepth: Int = 0,
    val startX: Double = 0.0,
    val startY: Double = 0.0,
    val startSpeed: Vector = CartesianVector(0.0, 0.0)
) {
    private var phisicReference: BounceablePoint = BounceablePoint(
        ((1 shl (maxDepth * 5)) shr (depth * 5)).toDouble(), // higher layer balls are heavier
        startSpeed,
        startX,
        startY
    )

    companion object{
        const val SMALL_RADIUS = 100.0
        const val OUTER_RADIUS_RATIO = 1.1
        const val LAYER_RATIO = 5.1

        fun makeBounce(one: Ball, two: Ball) {
            val oneNewSpeed = one.phisicReference.bounceOn(two.phisicReference)
            val twoNewSpeed = two.phisicReference.bounceOn(one.phisicReference)

            one.phisicReference = oneNewSpeed
            two.phisicReference = twoNewSpeed
        }
    }

    private val innerRadius = SMALL_RADIUS * Math.pow(LAYER_RATIO, -depth.toDouble())
    private val outerRadius = innerRadius * OUTER_RADIUS_RATIO

    private val x: Double by this
    private val y: Double by this

    operator fun getValue(ball: Ball, property: KProperty<*>): Double {
        when (property.name) {
            "x" -> return phisicReference.x
            "y" -> return phisicReference.y
            else -> throw IllegalArgumentException("Unknown property ${property.name}")
        }
    }

    private val children = BallGenerator(
        numberOfChildren,
        x,
        y,
        SMALL_RADIUS * Math.pow(LAYER_RATIO, -(depth.toDouble() + 1)) * OUTER_RADIUS_RATIO,
        innerRadius
    ).getBalls().map {
        Ball(
            numberOfChildren = numberOfChildren,
            depth = depth + 1,
            maxDepth = maxDepth,
            startX = it.first,
            startY = it.second,
            startSpeed = phisicReference.speed / LAYER_RATIO * Random.nextDouble(0.9, 1.2)
        )
    }

    fun checkCollisionsAndUpdate(){
        val alreadyChecked = mutableSetOf<Pair<Ball, Ball>>()

        for (child in children){
            child.checkCollisionsAndUpdate()

            if (child.isCollidingWith(this)){
                makeBounce(this, child)
            }
            for (other in children){
                if (child != other && !alreadyChecked.contains(Pair(child, other))){
                    if (child.isCollidingWith(other)){
                        makeBounce(child, other)
                    }
                    alreadyChecked.add(Pair(child, other))
                    alreadyChecked.add(Pair(other, child))
                }
            }
        }
    }

    private fun isCollidingWith(other: Ball): Boolean{
        when {
            depth == other.depth -> return isCollidingWithBorderOF(other)
            depth > other.depth -> return isCollidingInternallyWith(other)
            else -> return other.isCollidingInternallyWith(this)
        }
    }

    private fun isCollidingWithBorderOF(other: Ball): Boolean{
        return Math.sqrt(Math.pow(x - other.x, 2.0) + Math.pow(y - other.y, 2.0)) < outerRadius + other.outerRadius
    }

    private fun isCollidingInternallyWith(other: Ball): Boolean{
        return Math.sqrt(Math.pow(x - other.x, 2.0) + Math.pow(y - other.y, 2.0)) + outerRadius > other.innerRadius
    }

    fun drawOn(sketch: PApplet) {
        sketch.fill(0)
        sketch.ellipse(x.toFloat(), y.toFloat(), (outerRadius * 2).toFloat(), (outerRadius * 2).toFloat())
        sketch.fill(255)
        sketch.ellipse(x.toFloat(), y.toFloat(), (innerRadius * 2).toFloat(), (innerRadius * 2).toFloat())

        children.forEach { it.drawOn(sketch) }
    }

    fun updateForwardOfTime(time: Double) {
        phisicReference = phisicReference.moveOfTime(time)
        children.forEach { it.updateForwardOfTime(time) }
    }

    fun checkForBounceOnBoundaryOf(width: Double, height: Double) {
        checkUpperBoundary()
        checkLowerBoundary(height)
        checkLeftBoundary()
        checkRightBoundary(width)
    }

    private fun checkUpperBoundary() {
        if (y - outerRadius < 0) {
            if (phisicReference.y < 0.0) {
                phisicReference = phisicReference.reverseY()
            } else {
                phisicReference += CartesianVector(0.0, 0.1)
            }
        }
    }

    private fun checkLowerBoundary(height: Double) {
        if (y + outerRadius > height) {
            if (phisicReference.y > 0.0) {
                phisicReference = phisicReference.reverseY()
            } else {
                phisicReference += CartesianVector(0.0, -0.1)
            }
        }
    }

    private fun checkLeftBoundary() {
        if (x - outerRadius < 0) {
            if (phisicReference.x < 0.0) {
                phisicReference = phisicReference.reverseX()
            } else {
                phisicReference += CartesianVector(0.1, 0.0)
            }
        }
    }

    private fun checkRightBoundary(width: Double) {
        if (x + outerRadius > width) {
            if (phisicReference.x > 0.0) {
                phisicReference = phisicReference.reverseX()
            } else {
                phisicReference += CartesianVector(-0.1, 0.0)
            }
        }
    }

}