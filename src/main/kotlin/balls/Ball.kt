package balls

import balls.childCarrier.BallsCarryCalculator
import balls.childCarrier.BruteInsideCarryCalculator
import processing.core.PApplet
import vectors.CartesianVector
import vectors.PolarVector
import vectors.Vector
import kotlin.math.*
import kotlin.random.Random
import kotlin.reflect.KProperty

private const val COLOR = 0
private const val SMALL_RADIUS = 160.0
private const val OUTER_RADIUS_RATIO = 1.1
private const val LAYER_RATIO = 4.5
private const val MASS_RATIO_POWER = 6
private const val BOUNCE_MULTIPLIER = 1.00001
private const val DECELERATION = 0.997

private const val CHECK_FUTURE_COLLISIONS = true


class Ball(
    private val numberOfChildren: Int = 0,
    private val depth: Int = 0,
    private val maxDepth: Int = 0,
    val startX: Double = 0.0,
    val startY: Double = 0.0,
    val startSpeed: Vector = CartesianVector(0.0, 0.0)
) {
    private var physicReference: BounceablePoint = BounceablePoint(
        ((1 shl (maxDepth * MASS_RATIO_POWER)) shr (depth * MASS_RATIO_POWER)).toDouble(), // higher layer balls are heavier
        startSpeed,
        startX,
        startY
    )

    val innerRadius = getInnerRadiusForDepth(depth)
    val outerRadius = getOuterRadiusForDepth(depth)

    val x: Double by this
    val y: Double by this

    operator fun getValue(ball: Ball, property: KProperty<*>): Double {
        return when (property.name) {
            "x" -> physicReference.x
            "y" -> physicReference.y
            else -> throw IllegalArgumentException("Unknown property ${property.name}")
        }
    }

    private val children = BallGenerator(
        if (depth < maxDepth) numberOfChildren else 0,
        x,
        y,
        getOuterRadiusForDepth(depth + 1),
        innerRadius * 2,
        innerRadius * 2
    ).getBalls().map {
        Ball(
            numberOfChildren = numberOfChildren,
            depth = depth + 1,
            maxDepth = maxDepth,
            startX = it.first,
            startY = it.second,
            startSpeed = physicReference.speed / LAYER_RATIO.pow(depth.toDouble() + 1) * Random.nextDouble(0.5, 1.2)
        )
    }

    private val carryCalculator: BallsCarryCalculator = BruteInsideCarryCalculator(children, this)
    private val alreadyCheckedPairs: MutableSet<Pair<Ball, Ball>> = HashSet()

    companion object{
        fun makeBounce(one: Ball, two: Ball) {
            val oneNewSpeed = one.physicReference.bounceOn(two.physicReference)
            val twoNewSpeed = two.physicReference.bounceOn(one.physicReference)

            one.physicReference = oneNewSpeed * BOUNCE_MULTIPLIER
            two.physicReference = twoNewSpeed * BOUNCE_MULTIPLIER
        }

        fun getInnerRadiusForDepth(depth: Int): Double{
            return SMALL_RADIUS * LAYER_RATIO.pow(-depth.toDouble())
        }

        fun getOuterRadiusForDepth(depth: Int): Double{
            return getInnerRadiusForDepth(depth) * OUTER_RADIUS_RATIO
        }
    }

    fun checkCollisionsAndUpdate(){
        alreadyCheckedPairs.clear()

        for (child in children){
            if (child.needToBounceWith(this)){
                makeBounce(this, child)
            }
            checkCollisionsWithSiblingsOf(child)
            child.checkCollisionsAndUpdate()
        }
    }

    private fun checkCollisionsWithSiblingsOf(child: Ball){
        for (other in children){
            if (child != other && Pair(child, other) !in alreadyCheckedPairs){
                if (child.needToBounceWith(other)){
                    makeBounce(child, other)
                }
                alreadyCheckedPairs.add(Pair(child, other))
                alreadyCheckedPairs.add(Pair(other, child))
            }
        }
    }

    fun needToBounceWith(other: Ball): Boolean{
        return hasCollisionWith(other) && isConvergentWith(other)
    }

    private fun hasCollisionWith(other: Ball): Boolean{
        return if (CHECK_FUTURE_COLLISIONS){
            willCollideWith(other)
        } else {
            isCollidingWith(other)
        }
    }

    private fun willCollideWith(other: Ball): Boolean {
        return when {
            depth == other.depth -> willCollideWithBorderOF(other)
            depth > other.depth -> willCollideWithInsideOf(other)
            else -> other.willCollideWithInsideOf(this)
        }
    }

    private fun willCollideWithBorderOF(other: Ball): Boolean {
        val thisNextPosition = physicReference.moveOfTime(1.0 / 60)
        val otherNextPosition = other.physicReference.moveOfTime(1.0 / 60)
        return sqrt(
            (thisNextPosition.x - otherNextPosition.x).pow(2.0) +
                    (thisNextPosition.y - otherNextPosition.y).pow(2.0)
        ) < outerRadius + other.outerRadius
    }

    private fun willCollideWithInsideOf(other: Ball): Boolean {
        val thisNextPosition = physicReference.moveOfTime(1.0 / 60.0)
        val otherNextPosition = other.physicReference.moveOfTime(1.0 / 60.0)
        return sqrt(
            (thisNextPosition.x - otherNextPosition.x).pow(2.0) +
                (thisNextPosition.y - otherNextPosition.y).pow(2.0)) +
                outerRadius > other.innerRadius
    }

    private fun isCollidingWith(other: Ball): Boolean{
        return when {
            depth == other.depth -> isCollidingWithBorderOf(other)
            depth > other.depth -> isCollidingWithTheInsideOf(other)
            else -> other.isCollidingWithTheInsideOf(this)
        }
    }

    fun isCollidingWithTheInsideOf(other: Ball): Boolean{
        if (depth != other.depth + 1){
            throw IllegalArgumentException("Cannot check inner collision between balls of depth $depth and ${other.depth}")
        }

        return sqrt((other.x - x).pow(2.0) + (other.y - y).pow(2.0)) + outerRadius > other.innerRadius
    }

    private fun isCollidingWithBorderOf(other: Ball): Boolean{
        if (depth != other.depth){
            throw IllegalArgumentException("Cannot check outer collision between balls of depth $depth and ${other.depth}")
        }

        return sqrt((other.x - x).pow(2.0) + (other.y - y).pow(2.0)) < outerRadius + other.outerRadius
    }

    private fun isConvergentWith(other: Ball): Boolean{
        return when {
            depth == other.depth -> isConvergentWithCenterOf(other)
            depth > other.depth -> isConvergentWithInternalBorderOf(other)
            else -> other.isConvergentWithInternalBorderOf(this)
        }
    }

    private fun isConvergentWithCenterOf(other: Ball): Boolean {
        return physicReference.isConvergentOn(other.physicReference)
    }

    private fun isConvergentWithInternalBorderOf(other: Ball): Boolean {
        val otherDirection = atan2(y - other.y, x - other.x)
        val otherReference = other.physicReference.moveOf(PolarVector(other.innerRadius, otherDirection))
        return physicReference.isConvergentOn(otherReference)
    }

    fun drawOn(sketch: PApplet) {
        sketch.stroke(COLOR)
        sketch.strokeWeight((outerRadius - innerRadius).toFloat())
        sketch.noFill()
        sketch.circle(x.toFloat(), y.toFloat(), (outerRadius + innerRadius).toFloat())

        children.forEach { it.drawOn(sketch) }
    }

    fun updateForwardOfTime(time: Double) {
        val oldSpeed = physicReference.speed * DECELERATION.pow(if (depth <= 1) 0.0 else depth.toDouble() - 1)

        physicReference = physicReference.moveOfTime(time)
        carryOnChildren()
        children.forEach { it.updateForwardOfTime(time) }
        carryOnChildren()

        physicReference = physicReference.withSpeed(oldSpeed)
    }

    private fun carryOnChildren(){
        val movesToDo = carryCalculator.computeRepositioningOfChildrenInParent()
        children.forEach {
            it.physicReference = it.physicReference.moveOf(movesToDo[it]!!)
            it.carryOnChildren()
        }
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
        return "Ball(depth=$depth) ${hashCode()}"
    }
}