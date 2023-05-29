package sketches

import balls.BallContainer
import processing.core.PApplet

class FirstSketch: PApplet() {
    private val WIDTH = 800
    private val HEIGHT = 600


    override fun settings() {
        size(WIDTH, HEIGHT)
    }

    private val container = BallContainer(
        height = HEIGHT.toDouble(),
        width = WIDTH.toDouble(),
        numberOfChild = 4,
        numberOfLayer = 2,
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