package vectors

interface Vector {
    fun add(v: Vector): Vector
    fun subtract(v: Vector): Vector
}