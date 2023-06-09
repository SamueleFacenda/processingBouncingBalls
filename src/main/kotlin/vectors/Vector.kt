package vectors

import kotlin.math.sqrt

interface Vector {
    fun add(v: Vector): Vector
    fun subtract(v: Vector): Vector
    fun multiply(s: Double): Vector
    fun divide(s: Double): Vector
    fun getX(): Double
    fun getY(): Double
    fun getLength(): Double {
        return sqrt(getX() * getX() + getY() * getY())
    }
    fun getProjectOn(dir: Double): Vector

    operator fun plus(v: Vector): Vector {
        return add(v)
    }

    operator fun minus(v: Vector): Vector {
        return subtract(v)
    }

    operator fun times(s: Double): Vector {
        return multiply(s)
    }

    operator fun div(s: Double): Vector {
        return divide(s)
    }
}