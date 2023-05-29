package balls

import kotlin.random.Random

class BallGenerator(
    private val numberOfBalls: Int,
    private val x: Double,
    private val y: Double,
    private val radius: Double,
    private val width: Double,
    private val height: Double,
    private val isCircle: Boolean = true
) {


    private val balls = mutableListOf<Pair<Double, Double>>()
    init {
        var tmpX: Double
        var tmpY: Double
        for (i in 0 until numberOfBalls){
            do {
                tmpX = x + Random.nextDouble(-width/2 + radius, width/2 - radius)
                tmpY = y + Random.nextDouble(-height/2 + radius, height/2 - radius)
            } while (!canFit(tmpX, tmpY))
            balls.add(Pair(tmpX, tmpY))
        }
        println("Generated ${balls.size} balls")
    }

    private fun canFit(x: Double, y: Double): Boolean{
        if (isCircle && Math.sqrt(Math.pow(x - this.x, 2.0) + Math.pow(y - this.y, 2.0)) + radius > width){
            return false
        }

        for (ball in balls){
            if (Math.sqrt(Math.pow(x - ball.first, 2.0) + Math.pow(y - ball.second, 2.0)) < radius * 2){
                return false
            }
        }
        return true
    }

    fun getBalls(): List<Pair<Double, Double>>{
        return balls
    }
}