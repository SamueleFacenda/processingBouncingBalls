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
        var tmpX = 0.0
        var tmpY = 0.0
        for (i in 0 until numberOfBalls){
            do {
                tmpX = Random.nextDouble(-outerRadius, outerRadius)
                tmpY = Random.nextDouble(-outerRadius, outerRadius)
            } while (!canFit(tmpX, tmpY))
            balls.add(Pair(tmpX, tmpY))
        }
    }

    private fun canFit(x: Double, y: Double): Boolean{
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