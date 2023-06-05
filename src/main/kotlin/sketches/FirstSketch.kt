package sketches

import balls.BallContainer
import processing.core.PApplet

private const val WIDTH = 1000
private const val HEIGHT = 800
private const val NUMBER_OF_CHILD = 4
private const val NUMBER_OF_LAYER = 3

object FirstSketch: PApplet() {

    override fun settings() {
        size(WIDTH, HEIGHT)
    }

    private val container = BallContainer(
        height = HEIGHT.toDouble(),
        width = WIDTH.toDouble(),
        numberOfChild = NUMBER_OF_CHILD,
        numberOfLayer = NUMBER_OF_LAYER,
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