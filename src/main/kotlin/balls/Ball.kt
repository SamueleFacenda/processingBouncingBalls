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
        ((1 shl (maxDepth * MASS_RATIO_POWER)) shr (depth * MASS_RATIO_POWER)).toDouble(), // higher layer balls are heavier
        startSpeed,
        startX,
        startY
    )

    companion object{
        const val SMALL_RADIUS = 120.0
        const val OUTER_RADIUS_RATIO = 1.1
        const val LAYER_RATIO = 5.1
        const val MASS_RATIO_POWER = 6

        fun makeBounce(one: Ball, two: Ball) {
            val oneNewSpeed = one.phisicReference.bounceOn(two.phisicReference)
            val twoNewSpeed = two.phisicReference.bounceOn(one.phisicReference)

            one.phisicReference = oneNewSpeed
            two.phisicReference = twoNewSpeed

            one.latestBouncesCounter[two] = 0
            two.latestBouncesCounter[one] = 0
        }

        fun getInnerRadiusForDepth(depth: Int): Double{
            return SMALL_RADIUS * Math.pow(LAYER_RATIO, -depth.toDouble())
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
        when (property.name) {
            "x" -> return phisicReference.x
            "y" -> return phisicReference.y
            else -> throw IllegalArgumentException("Unknown property ${property.name}")
        }
    }

    private val children = BallGenerator(
        if (depth < maxDepth) numberOfChildren else 0,
        x,
        y,
        getOuterRadiusForDepth(depth + 1),
        innerRadius,
        innerRadius
    ).getBalls().map {
        Ball(
            numberOfChildren = numberOfChildren,
            depth = depth + 1,
            maxDepth = maxDepth,
            startX = it.first,
            startY = it.second,
            startSpeed = phisicReference.speed / Math.pow(LAYER_RATIO, depth.toDouble() + 1) * Random.nextDouble(0.5, 1.2)
        )
    }

    private val latestBouncesCounter = mutableMapOf<Ball, Int>()
    private val nextCollisionsDirections = mutableSetOf<Double>()

    fun checkCollisionsAndUpdate(){
        updateDebouncerMap()
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

    private fun updateDebouncerMap() {
        val iterator = latestBouncesCounter.iterator()
        while (iterator.hasNext()){
            val entry = iterator.next()
            if (entry.value > 5){
                iterator.remove()
            } else {
                latestBouncesCounter[entry.key] = entry.value + 1
            }
        }
    }

    fun isCollidingWith(other: Ball): Boolean{
        if (other in latestBouncesCounter){
            return false
        }

        when {
            depth == other.depth -> return willCollidingWithBorderOF(other)
            depth > other.depth -> return willCollidingInternallyWith(other)
            else -> return other.willCollidingInternallyWith(this)
        }
    }

    private fun willCollidingWithBorderOF(other: Ball): Boolean{
        val nextPosition = phisicReference.moveOfTime(1.0 / 60.0)
        val willCollide = Math.sqrt(Math.pow(nextPosition.x - other.x, 2.0) + Math.pow(nextPosition.y - other.y, 2.0)) < outerRadius + other.outerRadius
        if (willCollide) {
            nextCollisionsDirections.add(Math.atan2(other.y - y, other.x - x))
        }
        return willCollide
    }

    private fun willCollidingInternallyWith(other: Ball): Boolean{
        val nextPosition = phisicReference.moveOfTime(1.0 / 60.0)
        val willCollide = Math.sqrt(Math.pow(nextPosition.x - other.x, 2.0) + Math.pow(nextPosition.y - other.y, 2.0)) + outerRadius > other.innerRadius
        if (willCollide) {
            nextCollisionsDirections.add(- Math.atan2(other.y - y, other.x - x))
        }
        return willCollide
    }

    fun drawOn(sketch: PApplet) {
        sketch.fill(0)
        sketch.ellipse(x.toFloat(), y.toFloat(), (outerRadius * 2).toFloat(), (outerRadius * 2).toFloat())
        sketch.fill(255)
        sketch.ellipse(x.toFloat(), y.toFloat(), (innerRadius * 2).toFloat(), (innerRadius * 2).toFloat())

        children.forEach { it.drawOn(sketch) }
    }

    fun updateForwardOfTime(time: Double) {
        val oldSpeed = phisicReference.speed

        // if there are collisions, we need to update the speed to block the movement in such directions
        if (nextCollisionsDirections.isNotEmpty()){
            var speedToUse = oldSpeed
            nextCollisionsDirections.forEach {
                val speedInDirection = speedToUse.projectOnDirection(it)
                speedToUse -= speedInDirection
            }
            phisicReference = phisicReference.withSpeed(speedToUse)
            nextCollisionsDirections.clear()
        }

        phisicReference = phisicReference.moveOfTime(time)
        children.forEach { it.updateForwardOfTime(time) }

        // restore the speed
        phisicReference = phisicReference.withSpeed(oldSpeed)
    }

    fun checkForBounceOnBoundaryOf(width: Double, height: Double) {
        checkUpperBoundary()
        checkLowerBoundary(height)
        checkLeftBoundary()
        checkRightBoundary(width)
    }

    private fun checkUpperBoundary() {
        if (y - outerRadius < 0) {
            if (phisicReference.speed.getY() < 0.0) {
                phisicReference = phisicReference.reverseY()
            } else {
                phisicReference += CartesianVector(0.0, 0.1)
            }
        }
    }

    private fun checkLowerBoundary(height: Double) {
        if (y + outerRadius > height) {
            if (phisicReference.speed.getY() > 0.0) {
                phisicReference = phisicReference.reverseY()
            } else {
                phisicReference += CartesianVector(0.0, -0.1)
            }
        }
    }

    private fun checkLeftBoundary() {
        if (x - outerRadius < 0) {
            if (phisicReference.speed.getX() < 0.0) {
                phisicReference = phisicReference.reverseX()
            } else {
                phisicReference += CartesianVector(0.1, 0.0)
            }
        }
    }

    private fun checkRightBoundary(width: Double) {
        if (x + outerRadius > width) {
            if (phisicReference.speed.getX() > 0.0) {
                phisicReference = phisicReference.reverseX()
            } else {
                phisicReference += CartesianVector(-0.1, 0.0)
            }
        }
    }

    fun checkCollisionsWith(other: List<Ball>){
        for (ball in other){
            if (isCollidingWith(ball)){
                makeBounce(this, ball)
            }
        }
    }

}