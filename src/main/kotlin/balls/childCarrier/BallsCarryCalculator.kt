package balls.childCarrier

import balls.Ball
import vectors.Vector

abstract class BallsCarryCalculator(
    protected val balls: List<Ball>,
    protected val parent: Ball
) {
    abstract fun computeRepositioningOfChildrenInParent(): Map<Ball, Vector>
}