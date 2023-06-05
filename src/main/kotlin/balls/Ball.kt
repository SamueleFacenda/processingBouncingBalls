package balls

import balls.childCarrier.BallsCarryCalculator
import balls.childCarrier.BruteInsideCarryCalculator
import processing.core.PApplet
import vectors.CartesianVector
import vectors.Vector
import kotlin.math.pow
import kotlin.math.sqrt
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
    private var physicReference: BounceablePoint = BounceablePoint(
        ((1 shl (maxDepth * MASS_RATIO_POWER)) shr (depth * MASS_RATIO_POWER)).toDouble(), // higher layer balls are heavier
        startSpeed,
        startX,
        startY
    )

    companion object{
        private const val SMALL_RADIUS = 180.0
        private const val OUTER_RADIUS_RATIO = 1.1
        const val LAYER_RATIO = 4.5
        const val MASS_RATIO_POWER = 6
        private const val BOUNCE_MULTIPLIER = 1.0
        const val DECELERATION = 0.997

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

    private val bounceInProgress: MutableSet<Ball> = HashSet()
    private val carryCalculator: BallsCarryCalculator = BruteInsideCarryCalculator(children, this)
    private val alreadyCheckedPairs: MutableSet<Pair<Ball, Ball>> = HashSet()

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
        var out = willCollideWith(other)
        if (out) {
            out = bounceInProgress.add(other)
            println("Bounce in progress($out) for $this, other: $other")
        } else {
            val contained = bounceInProgress.remove(other)
            if (contained) println("contained for $this, other: $other")
        }
        return out
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

    fun isCollidingInternallyWith(other: Ball): Boolean{
        if (depth != other.depth - 1){
            throw IllegalArgumentException("Cannot check inner collision between balls of depth $depth and ${other.depth}")
        }

        return sqrt((other.x - x).pow(2.0) + (other.y - y).pow(2.0)) + other.outerRadius > innerRadius
    }

    private fun isCollidingWithBorderOf(other: Ball): Boolean{
        if (depth != other.depth){
            throw IllegalArgumentException("Cannot check outer collision between balls of depth $depth and ${other.depth}")
        }

        return sqrt((other.x - x).pow(2.0) + (other.y - y).pow(2.0)) < outerRadius + other.outerRadius
    }

    fun drawOn(sketch: PApplet) {
        sketch.fill(0)
        sketch.ellipse(x.toFloat(), y.toFloat(), (outerRadius * 2).toFloat(), (outerRadius * 2).toFloat())
        sketch.fill(255)
        sketch.ellipse(x.toFloat(), y.toFloat(), (innerRadius * 2).toFloat(), (innerRadius * 2).toFloat())

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