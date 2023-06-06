package balls.childCarrier

import balls.BounceableBall
import vectors.CartesianVector
import vectors.PolarVector
import vectors.Vector
import kotlin.math.*

class BruteInsideCarryCalculator(balls: List<BounceableBall>, parent: BounceableBall): BallsCarryCalculator(balls, parent) {

    private var ballsMovements = mutableMapOf<BounceableBall, Vector>()

    override fun computeRepositioningOfChildrenInParent(): Map<BounceableBall, Vector> {
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

    private fun getCarriedPosition(child: BounceableBall): Vector {
        val direction = atan2(child.y - parent.y, child.x - parent.x)
        val wantedDistance = parent.innerRadius - child.outerRadius
        val distance = sqrt((parent.x - child.x).pow(2.0) + (parent.y - child.y).pow(2.0))
        val distanceToMove = wantedDistance - distance

        return PolarVector(distanceToMove, direction)
    }

}