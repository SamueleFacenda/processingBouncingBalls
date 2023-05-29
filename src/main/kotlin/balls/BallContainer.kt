package balls

import vectors.PolarVector
import kotlin.random.Random

class BallContainer(
    private val height: Double = 0.0,
    private val width: Double = 0.0,
    private val numberOfChild: Int = 1,
    private val numberOfLayer: Int = 1
) {
    private val balls: List<Ball> = (1..numberOfChild).map { getNewBall(it) }

    private fun getNewBall(index: Int): Ball{
        return Ball(
            numberOfChildren = numberOfChild,
            layer = numberOfLayer,
            startX = width / 2,
            startY = height / 2,
            startSpeed = PolarVector(
                Random.nextDouble(0.0, 10.0),
                Random.nextDouble(0.0, 2 * Math.PI)
            )
        )
    }
}