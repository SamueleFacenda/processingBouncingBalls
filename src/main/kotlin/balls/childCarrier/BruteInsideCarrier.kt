package balls.childCarrier

import balls.Ball
import vectors.CartesianVector
import vectors.PolarVector
import vectors.Vector
import kotlin.math.*

class BruteInsideCarrier(balls: List<Ball>, parent: Ball): BallsCarryCalculator(balls, parent) {

    private var ballsPositions = mutableMapOf<Ball, Vector>()

    override fun computeRepositioningOfChildrenInParent(): Map<Ball, Vector> {
        ballsPositions.clear()

        balls.forEach {
            if (parent.isCollidingInternallyWith(it)) {
                ballsPositions[it] = getCarriedPosition(it)
            } else {
                ballsPositions[it] = CartesianVector(0.0, 0.0)
            }
        }

        return ballsPositions
    }

    private fun getCarriedPosition(child: Ball): Vector {
        val direction = atan2(child.y - parent.y, child.x - parent.x)
        val wantedDistance = parent.innerRadius - child.outerRadius
        val distance = sqrt((parent.x - child.x).pow(2.0) + (parent.y - child.y).pow(2.0))
        val distanceToMove = wantedDistance - distance

        return PolarVector(distanceToMove, direction)
    }

}