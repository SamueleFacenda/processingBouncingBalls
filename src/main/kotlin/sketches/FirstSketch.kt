package sketches

import balls.BallContainer
import processing.core.PApplet

object FirstSketch: PApplet() {
    private const val WIDTH = 1000
    private const val HEIGHT = 800


    override fun settings() {
        size(WIDTH, HEIGHT)
    }

    private val container = BallContainer(
        height = HEIGHT.toDouble(),
        width = WIDTH.toDouble(),
        numberOfChild = 3,
        numberOfLayer = 4,
        sketch = this
    )

    override fun setup() {
        background(255)
    }

    override fun draw() {
        background(255)
        fill(0)

        container.update()
        container.draw()
    }
}