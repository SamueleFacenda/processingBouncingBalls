package balls

import vectors.CartesianVector
import vectors.Vector

class BounceablePoint(
    private val mass: Double = 0.0,
    private val speed: Vector = CartesianVector(0.0, 0.0),
    val x: Double = 0.0,
    val y: Double = 0.0
) {

    fun moveOfTime(time: Double): BounceablePoint {
        val newX = x + speed.getX() * time
        val newY = y + speed.getY() * time
        return BounceablePoint(mass, speed, newX, newY)
    }

    fun bounceOn(other: BounceablePoint): BounceablePoint {
        TODO("studiare un po di fisica e implementare la funzione")
    }
}