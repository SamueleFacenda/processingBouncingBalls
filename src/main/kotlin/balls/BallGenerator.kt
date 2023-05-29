package balls

import kotlin.random.Random

class BallGenerator(
    private val numberOfBalls: Int,
    private val x: Double,
    private val y: Double,
    private val radius: Double,
    private val outerRadius: Double,
) {
    private val balls = mutableListOf<Pair<Double, Double>>()
    init {
        println("Generating $numberOfBalls balls, radius $radius, outer radius $outerRadius")
        var tmpX: Double
        var tmpY: Double
        for (i in 0 until numberOfBalls){
            do {
                tmpX = this.x + Random.nextDouble(-outerRadius + radius, outerRadius - radius)
                tmpY = this.y + Random.nextDouble(-outerRadius + radius, outerRadius - radius)
            } while (!canFit(tmpX, tmpY))
            balls.add(Pair(tmpX, tmpY))
        }
    }

    private fun canFit(x: Double, y: Double): Boolean{
        if (Math.sqrt(Math.pow(x - this.x, 2.0) + Math.pow(y - this.y, 2.0)) + radius > outerRadius){
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