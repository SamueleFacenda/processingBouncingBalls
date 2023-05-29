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
    private val balls: List<Ball> = BallGenerator(
        numberOfChild,
        width / 2.0,
        height / 2.0,
        Ball.getOuterRadiusForDepth(0),
        width,
        height,
        false
    ).getBalls().map {
        Ball(
            numberOfChildren = numberOfChild,
            depth = 0,
            maxDepth = maxDepth,
            startX = it.first,
            startY = it.second,
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