package balls.childCarrier

import balls.Ball

abstract class BallsCarryCalculator(
    protected val balls: List<Ball>,
    protected val parent: Ball
) {
    abstract fun computeRepositioningOfChildrenInParent(): Map<Ball, Pair<Double, Double>>
}