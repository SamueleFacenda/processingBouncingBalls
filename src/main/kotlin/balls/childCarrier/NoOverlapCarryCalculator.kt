package balls.childCarrier

import balls.BounceableBall
import vectors.Vector
import kotlin.math.pow
import kotlin.math.sqrt

class NoOverlapCarryCalculator(
    balls: List<BounceableBall>,
    parent: BounceableBall
) : BallsCarryCalculator(balls, parent) {

    private var toMove = ArrayDeque<BounceableBall>()
    private var moved = ArrayDeque<BounceableBall>()
    private val ballsMovements = mutableMapOf<BounceableBall, Vector>()

    override fun computeRepositioningOfChildrenInParent(): Map<BounceableBall, Vector> {
        ballsMovements.clear()
        moved.clear()
        sortBallByDistanceToParent()
        computeMovementsStartingFromFurther()
        return ballsMovements
    }

    private fun sortBallByDistanceToParent() {
        toMove = ArrayDeque(balls.sortedBy { ball ->
            sqrt((parent.x - ball.x).pow(2.0) + (parent.y - ball.y).pow(2.0))
        })
    }

    private fun computeMovementsStartingFromFurther() {
        var tmp: BounceableBall
        while(toMove.isNotEmpty()) {
            tmp = toMove.removeLast()
            arrangeBall(tmp)
            moved += tmp
        }
    }

    private fun arrangeBall(ball: BounceableBall){

    }

}