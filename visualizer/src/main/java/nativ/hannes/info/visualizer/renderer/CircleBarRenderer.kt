package nativ.hannes.info.visualizer.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import nativ.hannes.info.visualizer.data.AudioData
import nativ.hannes.info.visualizer.data.FFTData
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.sin

/**
 * Renders the FFT data onto a pulsing, rotating circle
 *
 * @param paint  - Paint to draw lines with
 */
class CircleBarRenderer constructor(private val paint: Paint, private val divisions: Int, private val cycleColor: Boolean = false) : Renderer() {

    // Do nothing, we only display FFT data
    override fun onRender(canvas: Canvas, data: AudioData, rect: Rect) = Unit

    override fun onRender(canvas: Canvas, data: FFTData, rect: Rect) {
        if (cycleColor) {
            cycleColor()
        }
        for (i in 0 until data.bytes.size / divisions) {
            // Calculate dbValue
            val rfk = data.bytes[divisions * i]
            val ifk = data.bytes[divisions * i + 1]
            val magnitude = (rfk * rfk + ifk * ifk).toFloat()
            val dbValue = 75 * log10(magnitude.toDouble()).toFloat()
            val cartPoint = floatArrayOf(
                    (i * divisions).toFloat() / (data.bytes.size - 1),
                    rect.height() / 2 - dbValue / 4
            )
            val polarPoint = toPolar(cartPoint, rect)
            fftPoints[i * 4] = polarPoint[0]
            fftPoints[i * 4 + 1] = polarPoint[1]
            val cartPoint2 = floatArrayOf(
                    (i * divisions).toFloat() / (data.bytes.size - 1),
                    rect.height() / 2 + dbValue
            )
            val polarPoint2 = toPolar(cartPoint2, rect)
            fftPoints[i * 4 + 2] = polarPoint2[0]
            fftPoints[i * 4 + 3] = polarPoint2[1]
        }
        canvas.drawLines(fftPoints, paint)

        // Controls the pulsing rate
        modulation += 0.13f
        angleModulation += 0.28f
    }

    private var modulation = 0f
    private var modulationStrength = 0.4f // 0-1
    private var angleModulation = 0f
    private var aggresive = 0.4f
    private fun toPolar(cartesian: FloatArray, rect: Rect): FloatArray {
        val cX = rect.width() / 2.toDouble()
        val cY = rect.height() / 2.toDouble()
        val angle = cartesian[0] * 2 * Math.PI
        val radius = (rect.width() / 2 * (1 - aggresive) + aggresive * cartesian[1] / 2) * (1 - modulationStrength + modulationStrength * (1 + sin(modulation.toDouble())) / 2)
        return floatArrayOf(
                (cX + radius * sin(angle + angleModulation)).toFloat(),
                (cY + radius * cos(angle + angleModulation)).toFloat()
        )
    }

    private var colorCounter = 0f
    private fun cycleColor() {
        val r = floor(128 * (sin(colorCounter.toDouble()) + 1)).toInt()
        val g = floor(128 * (sin(colorCounter + 2.toDouble()) + 1)).toInt()
        val b = floor(128 * (sin(colorCounter + 4.toDouble()) + 1)).toInt()
        paint.color = Color.argb(128, r, g, b)
        colorCounter += 0.03f
    }
    /**
     * Renders the audio data onto a pulsing circle
     *
     * @param paint      - Paint to draw lines with
     * @param divisions  - must be a power of 2. Controls how many lines to draw
     * @param cycleColor - If true the color will change on each frame
     */
}