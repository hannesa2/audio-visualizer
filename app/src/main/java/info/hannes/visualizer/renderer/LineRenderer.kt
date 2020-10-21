package info.hannes.visualizer.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import info.hannes.visualizer.data.AudioData
import info.hannes.visualizer.data.FFTData
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

/**
 * Renders the audio data onto a line. The line flashes on prominent beats
 *
 * @param paint - Paint to draw lines with
 * @param paint - Paint to draw flash with
 */
class LineRenderer constructor(private val paint: Paint, private val flashPaint: Paint, private val cycleColor: Boolean = false) : Renderer() {

    private var amplitude = 0f

    override fun onRender(canvas: Canvas, data: AudioData, rect: Rect) {
        if (cycleColor) {
            cycleColor()
        }

        // Calculate points for line
        for (i in 0 until data.bytes.size - 1) {
            mPoints[i * 4] = (rect.width() * i / (data.bytes.size - 1)).toFloat()
            mPoints[i * 4 + 1] = (rect.height() / 2 + (data.bytes[i] + 128).toByte() * (rect.height() / 3) / 128).toFloat()
            mPoints[i * 4 + 2] = (rect.width() * (i + 1) / (data.bytes.size - 1)).toFloat()
            mPoints[i * 4 + 3] = (rect.height() / 2 + (data.bytes[i + 1] + 128).toByte() * (rect.height() / 3) / 128).toFloat()
        }

        // Calc amplitude for this waveform
        var accumulator = 0f
        for (i in 0 until data.bytes.size - 1) {
            accumulator += abs(data.bytes[i].toFloat())
        }
        val amp: Float = accumulator / (128 * data.bytes.size)
        if (amp > amplitude) {
            // Amplitude is bigger than normal, make a prominent line
            amplitude = amp
            canvas.drawLines(mPoints, flashPaint)
        } else {
            // Amplitude is nothing special, reduce the amplitude
            amplitude *= 0.99f
            canvas.drawLines(mPoints, paint)
        }
    }

    // Do nothing, we only display audio data
    override fun onRender(canvas: Canvas, data: FFTData, rect: Rect) = Unit

    private var colorCounter = 0f
    private fun cycleColor() {
        val r = floor(128 * (sin(colorCounter.toDouble()) + 3)).toInt()
        val g = floor(128 * (sin(colorCounter + 1.toDouble()) + 1)).toInt()
        val b = floor(128 * (sin(colorCounter + 7.toDouble()) + 1)).toInt()
        paint.color = Color.argb(128, r, g, b)
        colorCounter += 0.03f
    }

}