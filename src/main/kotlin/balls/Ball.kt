package balls

import processing.core.PApplet
import vectors.CartesianVector
import vectors.PolarVector
import vectors.Vector
import kotlin.math.atan2
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
        const val DEBOUNCER = 5
        private const val BOUNCE_MULTIPLIER = 1.0
        const val DECELERATION = 0.997

        fun makeBounce(one: Ball, two: Ball) {
            val oneNewSpeed = one.physicReference.bounceOn(two.physicReference)
            val twoNewSpeed = two.physicReference.bounceOn(one.physicReference)

            one.physicReference = oneNewSpeed * BOUNCE_MULTIPLIER
            two.physicReference = twoNewSpeed * BOUNCE_MULTIPLIER

            one.latestBouncesCounter[two] = 0
            two.latestBouncesCounter[one] = 0
        }

        fun getInnerRadiusForDepth(depth: Int): Double{
            return SMALL_RADIUS * LAYER_RATIO.pow(-depth.toDouble())
        }

        fun getOuterRadiusForDepth(depth: Int): Double{
            return getInnerRadiusForDepth(depth) * OUTER_RADIUS_RATIO
        }
    }

    private val innerRadius = getInnerRadiusForDepth(depth)
    private val outerRadius = getOuterRadiusForDepth(depth)

    private val x: Double by this
    private val y: Double by this

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

    private val latestBouncesCounter = mutableMapOf<Ball, Int>()
    private val nextCollisionsDirections = mutableSetOf<Double>()

    fun checkCollisionsAndUpdate(){
        updateDebouncerMap()
        val alreadyChecked = mutableSetOf<Pair<Ball, Ball>>()

        for (child in children){
            child.checkCollisionsAndUpdate()

            if (child.willCollideWith(this)){
                makeBounce(this, child)
            }
            for (other in children){
                if (child != other && !alreadyChecked.contains(Pair(child, other))){
                    if (child.willCollideWith(other)){
                        makeBounce(child, other)
                    }
                    alreadyChecked.add(Pair(child, other))
                    alreadyChecked.add(Pair(other, child))
                }
            }
        }
    }

    private fun updateDebouncerMap() {
        val iterator = latestBouncesCounter.iterator()
        while (iterator.hasNext()){
            val entry = iterator.next()
            if (entry.value > DEBOUNCER){
                iterator.remove()
            } else {
                latestBouncesCounter[entry.key] = entry.value + 1
            }
        }
    }

    fun willCollideWith(other: Ball): Boolean{
        return when {
            depth == other.depth -> willCollideWithBorderOF(other)
            depth > other.depth -> willCollideWithInsideOf(other)
            else -> other.willCollideWithInsideOf(this)
        } && other !in latestBouncesCounter
    }

    private fun willCollideWithBorderOF(other: Ball): Boolean{
        val nextPosition = physicReference.moveOfTime(1.0 / 60.0)
        val willCollide = sqrt((nextPosition.x - other.x).pow(2.0) + (nextPosition.y - other.y).pow(2.0)) < outerRadius + other.outerRadius
        if (willCollide) {
            nextCollisionsDirections.add(atan2(other.y - y, other.x - x))
        }
        return willCollide
    }

    private fun willCollideWithInsideOf(other: Ball): Boolean{
        val nextPosition = physicReference.moveOfTime(1.0 / 60.0)
        val willCollide = sqrt((nextPosition.x - other.x).pow(2.0) + (nextPosition.y - other.y).pow(2.0)) + outerRadius > other.innerRadius
        if (willCollide) {
            nextCollisionsDirections.add(-atan2(other.y - y, other.x - x))
        }
        return willCollide
    }

    private fun isCollidingInternallyWith(other: Ball): Boolean{
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
        // removeObstacleDirectionsFromSpeed()

        physicReference = physicReference.moveOfTime(time)
        carryOnChildren()
        children.forEach { it.updateForwardOfTime(time) }
        carryOnChildren()

        physicReference = physicReference.withSpeed(oldSpeed)
    }

    private fun removeObstacleDirectionsFromSpeed() {
        if (nextCollisionsDirections.isNotEmpty()){
            var speedToUse = physicReference.speed
            nextCollisionsDirections.forEach {
                val speedInDirection = speedToUse.projectOnDirection(it)
                speedToUse -= speedInDirection
            }
            physicReference = physicReference.withSpeed(speedToUse)
            nextCollisionsDirections.clear()
        }
    }

    private fun carryOnChildren(){
        children.forEach {
            if (isCollidingInternallyWith(it)){
                carryOnChild(it)
                it.carryOnChildren()
            }
        }
    }

    private fun carryOnChild(child: Ball){
        val direction = atan2(child.y - y, child.x - x)
        val wantedDistance = innerRadius - child.outerRadius
        val distance = sqrt((x - child.x).pow(2.0) + (y - child.y).pow(2.0))
        val distanceToMove = wantedDistance - distance

        child.physicReference = child.physicReference.moveOf(PolarVector(distanceToMove, direction))
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

}