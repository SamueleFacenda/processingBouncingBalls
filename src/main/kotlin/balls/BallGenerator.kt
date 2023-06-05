package balls

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

private const val MAX_TRIES = 1000
private const val MAX_NEW_LIST_RETRIES = 30

class BallGenerator(
    private val numberOfBalls: Int,
    private val x: Double,
    private val y: Double,
    private val radius: Double,
    private val width: Double,
    private val height: Double,
    private val isCircle: Boolean = true,
    keepBest: Boolean = true
) {

    private val balls = mutableListOf<Pair<Double, Double>>()
    init {
        val bestBalls = mutableListOf<Pair<Double, Double>>()
        var i = 0
        while(i < MAX_NEW_LIST_RETRIES && !tryToPopulateList()){
            if (keepBest && balls.size > bestBalls.size){
                bestBalls.clear()
                bestBalls.addAll(balls)
            }
            balls.clear()
            i++
        }

        if (i == MAX_NEW_LIST_RETRIES) {
            if (keepBest){
                balls.addAll(bestBalls)
            } else {
                throw Exception("Could not generate a list of balls, even after $MAX_NEW_LIST_RETRIES tries")
            }
        }

    }

    private fun tryToPopulateList(): Boolean{
        var tmpX: Double
        var tmpY: Double
        var numberOfTries: Int
        for (i in 0 until numberOfBalls){
            numberOfTries = 0
            do {
                tmpX = x + Random.nextDouble(-width/2 + radius, width/2 - radius)
                tmpY = y + Random.nextDouble(-height/2 + radius, height/2 - radius)
                numberOfTries++
            } while (!canFit(tmpX, tmpY) && numberOfTries < MAX_TRIES)

            if (numberOfTries >= MAX_TRIES){
                return false
            }

            balls.add(Pair(tmpX, tmpY))
        }

        return true
    }

    private fun canFit(x: Double, y: Double): Boolean{
        if (isCircle && sqrt((x - this.x).pow(2.0) + (y - this.y).pow(2.0)) + radius > width/2){
            return false
        }

        for (ball in balls){
            if (sqrt((x - ball.first).pow(2.0) + (y - ball.second).pow(2.0)) < radius * 2){
                return false
            }
        }
        return true
    }

    fun getBalls(): List<Pair<Double, Double>>{
        return balls
    }
}