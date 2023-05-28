package vectors

interface Vector {
    fun add(v: Vector): Vector
    fun subtract(v: Vector): Vector
    fun multiply(s: Double): Vector
    fun divide(s: Double): Vector
}