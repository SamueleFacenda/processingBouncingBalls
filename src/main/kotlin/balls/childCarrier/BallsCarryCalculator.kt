package balls.childCarrier

import balls.BounceableBall
import vectors.Vector

abstract class BallsCarryCalculator(
    protected val balls: List<BounceableBall>,
    protected val parent: BounceableBall
) {
    abstract fun computeRepositioningOfChildrenInParent(): Map<BounceableBall, Vector>
}