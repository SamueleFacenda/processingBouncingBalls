package balls

import vectors.CartesianVector
import vectors.Vector

abstract class BounceablePoint {
    private val mass: Double = 0.0
    private val speed: Vector = CartesianVector(0.0, 0.0)
    private val x = 0.0
    private val y = 0.0

}