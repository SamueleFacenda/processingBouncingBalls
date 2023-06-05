package balls.childCarrier

import balls.Ball
import vectors.Vector
import kotlin.math.pow
import kotlin.math.sqrt

class NoOverlapCarryCalculator(
    balls: List<Ball>,
    parent: Ball
) : BallsCarryCalculator(balls, parent) {

    private var sortedBalls: List<Ball> = balls
    private val ballsMovements = mutableMapOf<Ball, Vector>()

    override fun computeRepositioningOfChildrenInParent(): Map<Ball, Vector> {
        ballsMovements.clear()
        sortBallByDistanceToParent()

        return ballsMovements
    }

    private fun sortBallByDistanceToParent() {
        sortedBalls = balls.sortedBy { ball ->
            -sqrt((parent.x - ball.x).pow(2.0) + (parent.y - ball.y).pow(2.0))
        }
    }


}