package sketches

import processing.core.PApplet

class FirstSketch: PApplet() {
    override fun settings() {
        size(800, 600)
    }

    override fun setup() {
        background(255)
    }

    override fun draw() {
        background(255)
        fill(0)
        ellipse(mouseX.toFloat(), mouseY.toFloat(), 50f, 50f)
    }
}