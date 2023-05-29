package balls

import processing.core.PApplet
import vectors.CartesianVector
import vectors.Vector
import kotlin.random.Random

class Ball(
    private val numberOfChildren: Int = 0,
    private val layer: Int = 0,
    val startX: Double = 0.0,
    val startY: Double = 0.0,
    val startSpeed: Vector = CartesianVector(0.0, 0.0)
) {
    private var phisicReference: BounceablePoint = BounceablePoint(
        (2 shl (layer * 3)).toDouble(), // higher layer balls are heavier
        startSpeed,
        startX,
        startY
    )

    companion object{
        const val SMALL_RADIUS = 10.0
        const val OUTER_RADIUS_RATIO = 1.1
        const val LAYER_RATIO = 3.1

        fun makeBounce(one: Ball, two: Ball) {
            val oneNewSpeed = one.phisicReference.bounceOn(two.phisicReference)
            val twoNewSpeed = two.phisicReference.bounceOn(one.phisicReference)

            one.phisicReference = oneNewSpeed
            two.phisicReference = twoNewSpeed
        }
    }

    private val innserRadius = SMALL_RADIUS * Math.pow(LAYER_RATIO, layer.toDouble())
    private val outerRadius = innserRadius * OUTER_RADIUS_RATIO

    private val x: Double by phisicReference::x
    private val y: Double by phisicReference::y

    private val children = List<Ball>(if (layer > 0) numberOfChildren else 0) { getNewChildren() }

    private fun getNewChildren(): Ball{
        return Ball(
            numberOfChildren = numberOfChildren,
            layer = layer - 1,
            startX = x,
            startY = y,
            startSpeed = phisicReference.speed / LAYER_RATIO * Random.nextDouble(51.3257, 52.4557)
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
            layer == other.layer -> return isCollidingInternallyWith(other)
            layer < other.layer -> return isCollidingWithBorderOF(other)
            else -> return other.isCollidingWithBorderOF(this)
        }
    }

    private fun isCollidingWithBorderOF(other: Ball): Boolean{
        return Math.sqrt(Math.pow(x - other.x, 2.0) + Math.pow(y - other.y, 2.0)) < outerRadius + other.outerRadius
    }

    private fun isCollidingInternallyWith(other: Ball): Boolean{
        return Math.sqrt(Math.pow(x - other.x, 2.0) + Math.pow(y - other.y, 2.0)) + outerRadius > other.innserRadius
    }

    fun drawOn(sketch: PApplet) {
        sketch.fill(0)
        sketch.ellipse(x.toFloat(), y.toFloat(), (outerRadius * 2).toFloat(), (outerRadius * 2).toFloat())
        sketch.fill(255)
        sketch.ellipse(x.toFloat(), y.toFloat(), (innserRadius * 2).toFloat(), (innserRadius * 2).toFloat())

        children.forEach { it.drawOn(sketch) }
    }

    fun updateForwardOfTime(time: Double) {
        phisicReference.moveOfTime(time)
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
            phisicReference = phisicReference.reverseY()
        }
    }

    private fun checkLowerBoundary(height: Double) {
        if (y + outerRadius > height) {
            phisicReference = phisicReference.reverseY()
        }
    }

    private fun checkLeftBoundary() {
        if (x - outerRadius < 0) {
            phisicReference = phisicReference.reverseX()
        }
    }

    private fun checkRightBoundary(width: Double) {
        if (x + outerRadius > width) {
            phisicReference = phisicReference.reverseX()
        }
    }
}