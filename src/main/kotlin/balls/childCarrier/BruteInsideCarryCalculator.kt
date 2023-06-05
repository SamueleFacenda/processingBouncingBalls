package balls.childCarrier

import balls.Ball
import vectors.CartesianVector
import vectors.PolarVector
import vectors.Vector
import kotlin.math.*

class BruteInsideCarryCalculator(balls: List<Ball>, parent: Ball): BallsCarryCalculator(balls, parent) {

    private var ballsMovements = mutableMapOf<Ball, Vector>()

    override fun computeRepositioningOfChildrenInParent(): Map<Ball, Vector> {
        ballsMovements.clear()

        balls.forEach {
            if (it.isCollidingWithTheInsideOf(parent)) {
                ballsMovements[it] = getCarriedPosition(it)
            } else {
                ballsMovements[it] = CartesianVector(0.0, 0.0)
            }
        }

        return ballsMovements
    }

    private fun getCarriedPosition(child: Ball): Vector {
        val direction = atan2(child.y - parent.y, child.x - parent.x)
        val wantedDistance = parent.innerRadius - child.outerRadius
        val distance = sqrt((parent.x - child.x).pow(2.0) + (parent.y - child.y).pow(2.0))
        val distanceToMove = wantedDistance - distance

        return PolarVector(distanceToMove, direction)
    }

}