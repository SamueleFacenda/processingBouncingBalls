package vectors

interface Vector {
    fun add(v: Vector): Vector
    fun subtract(v: Vector): Vector
    fun multiply(s: Double): Vector
    fun divide(s: Double): Vector
    fun getX(): Double
    fun getY(): Double
    fun getLength(): Double {
        return Math.sqrt(getX() * getX() + getY() * getY())
    }

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