package sketches

import balls.BallContainer
import processing.core.PApplet

private const val WIDTH = 1000
private const val HEIGHT = 800
private const val NUMBER_OF_CHILD = 5
private const val NUMBER_OF_LAYER = 3
private const val SAVE_FRAMES = false
private const val TIME_VIDEO_LENGTH = 10.0

object FirstSketch: PApplet() {

    override fun settings() {
        size(WIDTH, HEIGHT)
    }

    private var container = getNewBallContainer()

    private fun getNewBallContainer(): BallContainer{
        return BallContainer(
            height = HEIGHT.toDouble(),
            width = WIDTH.toDouble(),
            numberOfChild = NUMBER_OF_CHILD,
            numberOfLayer = NUMBER_OF_LAYER,
            sketch = this
        )
    }

    override fun mousePressed() {
        container = getNewBallContainer()
    }

    override fun setup() {
        background(255)
    }

    override fun draw() {
        background(255)
        fill(0)

        container.update()
        container.draw()

        if (SAVE_FRAMES){
            saveFrame("media/frames/####.png")
            println("fps = $frameRate")
            if (frameCount >= TIME_VIDEO_LENGTH * frameRate){
                exit()
            }
        }
    }
}