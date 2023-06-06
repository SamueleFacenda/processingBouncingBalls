package balls

import balls.childCarrier.BallsCarryCalculator
import balls.childCarrier.BruteInsideCarryCalculator
import processing.core.PApplet
import vectors.CartesianVector
import vectors.Vector
import kotlin.math.pow
import kotlin.random.Random

private const val COLOR = 0
private const val SMALL_RADIUS = 160.0
private const val OUTER_RADIUS_RATIO = 1.15
private const val LAYER_RATIO = 4.5
private const val MASS_RATIO_POWER = 5
private const val BOUNCE_MULTIPLIER = 1.1
private const val DECELERATION = 0.997

private const val STROBO_COLOR = true

class Ball(
    val numberOfChildren: Int = 0,
    private val depth: Int = 0,
    private val maxDepth: Int = 0,
    val startX: Double = 0.0,
    val startY: Double = 0.0,
    val startSpeed: Vector = CartesianVector(0.0, 0.0)
): BounceableBall(
    BounceablePoint(
        ((1 shl (maxDepth * MASS_RATIO_POWER)) shr (depth * MASS_RATIO_POWER)).toDouble(), // higher layer balls are heavier
        startSpeed,
        startX,
        startY
    ),
    depth,
    getInnerRadiusForDepth(depth),
    getOuterRadiusForDepth(depth),
) {

    private val children = BallGenerator(
        if (depth < maxDepth) numberOfChildren else 0,
        x,
        y,
        getOuterRadiusForDepth(depth + 1),
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
    private var color = if (STROBO_COLOR) getRandomColor() else COLOR

    companion object{
        fun makeBounce(one: Ball, two: Ball) {
            val oneNewSpeed = one.physicReference.bounceOn(two.physicReference)
            val twoNewSpeed = two.physicReference.bounceOn(one.physicReference)

            one.physicReference = oneNewSpeed * (if (one.depth == two.depth && one.depth == 0) BOUNCE_MULTIPLIER else 1.0)
            two.physicReference = twoNewSpeed * (if (one.depth == two.depth && one.depth == 0) BOUNCE_MULTIPLIER else 1.0)

            if (STROBO_COLOR){
                one.color = getRandomColor()
                two.color = getRandomColor()
            }
        }

        fun getInnerRadiusForDepth(depth: Int): Double{
            return SMALL_RADIUS * LAYER_RATIO.pow(-depth.toDouble())
        }

        fun getOuterRadiusForDepth(depth: Int): Double{
            return getInnerRadiusForDepth(depth) * OUTER_RADIUS_RATIO
        }

        fun getRandomColor(): Int{
            return (255 shl 24) or // alpha
                    (Random.nextInt(100, 255) shl 16) or // R
                    (Random.nextInt(100, 255) shl 8) or  // G
                    Random.nextInt(100, 255) // B
        }
    }

    fun checkCollisionsAndBounce(){
        alreadyCheckedPairs.clear()

        for (child in children){
            if (child.needToBounceWith(this)){
                makeBounce(this, child)
            }
            checkCollisionsWithSiblingsOf(child)
            child.checkCollisionsAndBounce()
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

    fun drawOn(sketch: PApplet) {
        sketch.stroke(color)
        sketch.strokeWeight((outerRadius - innerRadius).toFloat())
        sketch.noFill()
        sketch.circle(x.toFloat(), y.toFloat(), (outerRadius + innerRadius).toFloat())

        children.forEach { it.drawOn(sketch) }
    }

    fun updateForwardOfTime(time: Double) {
        decelerate()
        physicReference = physicReference.moveOfTime(time)
        children.forEach { it.updateForwardOfTime(time) }
        carryOnChildren()
    }

    private fun decelerate() {
        physicReference *= DECELERATION.pow(if (depth <= 1) 0.0 else depth.toDouble() - 1)
    }

    private fun carryOnChildren(){
        val movesToDo = carryCalculator.computeRepositioningOfChildrenInParent()
        children.forEach {
            it.physicReference = it.physicReference.moveOf(movesToDo[it]!!)
            it.carryOnChildren()
        }
    }

}