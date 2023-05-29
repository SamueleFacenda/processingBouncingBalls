package balls

import processing.core.PApplet
import vectors.PolarVector
import kotlin.random.Random

class BallContainer(
    private val height: Double = 0.0,
    private val width: Double = 0.0,
    private val numberOfChild: Int = 1,
    numberOfLayer: Int = 1,
    private val sketch: PApplet
) {
    private val maxDepth: Int = numberOfLayer - 1
    private val balls: List<Ball> = (1..numberOfChild).map { getNewBall() }

    private fun getNewBall(): Ball{
        return Ball(
            numberOfChildren = numberOfChild,
            depth = 0,
            maxDepth = maxDepth,
            startX = Random.nextDouble(0.0, width),
            startY = Random.nextDouble(0.0, height),
            startSpeed = PolarVector(
                Random.nextDouble(20.0, 50.0),
                Random.nextDouble(0.0, 2 * Math.PI)
            )
        )
    }

    fun update(){
        balls.forEach { it.checkCollisionsAndUpdate() }
        balls.forEach { it.updateForwardOfTime(1.0 / sketch.frameRate.toDouble()) }
        balls.forEach { it.checkForBounceOnBoundaryOf(width, height) }
    }

    fun draw(){
        balls.forEach { it.drawOn(sketch) }
    }
}