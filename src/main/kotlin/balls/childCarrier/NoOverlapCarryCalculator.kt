package balls.childCarrier

import balls.Ball
import vectors.Vector
import java.util.Deque
import java.util.Queue
import kotlin.math.pow
import kotlin.math.sqrt

class NoOverlapCarryCalculator(
    balls: List<Ball>,
    parent: Ball
) : BallsCarryCalculator(balls, parent) {

    private var toMove = ArrayDeque<Ball>()
    private var moved = ArrayDeque<Ball>()
    private val ballsMovements = mutableMapOf<Ball, Vector>()

    override fun computeRepositioningOfChildrenInParent(): Map<Ball, Vector> {
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
        var tmp: Ball
        while(toMove.isNotEmpty()) {
            tmp = toMove.removeLast()
            arrangeBall(tmp)
            moved += tmp
        }
    }

    private fun arrangeBall(ball: Ball){

    }

}