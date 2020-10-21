package info.hannes.visualizer.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import info.hannes.visualizer.data.AudioData
import info.hannes.visualizer.data.FFTData
import kotlin.math.log10

/**
 * Renders the FFT data as a series of lines, in histogram form
 *
 * @param divisions - must be a power of 2. Controls how many lines to draw
 * @param paint     - Paint to draw lines with
 * @param top       - whether to draw the lines at the top of the canvas, or the bottom
 */
class BarGraphRenderer(private val divisions: Int, private val paint: Paint, private val top: Boolean) : Renderer() {

    // Do nothing, we only display FFT data
    override fun onRender(canvas: Canvas, data: AudioData, rect: Rect) = Unit

    override fun onRender(canvas: Canvas, data: FFTData, rect: Rect) {
        for (i in 0 until data.bytes.size / divisions) {
            mFFTPoints[i * 4] = (i * 4 * divisions).toFloat()
            mFFTPoints[i * 4 + 2] = (i * 4 * divisions).toFloat()
            val rfk = data.bytes[divisions * i]
            val ifk = data.bytes[divisions * i + 1]
            val magnitude = (rfk * rfk + ifk * ifk).toFloat()
            val dbValue = (10 * log10(magnitude.toDouble())).toInt()
            if (top) {
                mFFTPoints[i * 4 + 1] = 0F
                mFFTPoints[i * 4 + 3] = (dbValue * 2 - 10).toFloat()
            } else {
                mFFTPoints[i * 4 + 1] = rect.height().toFloat()
                mFFTPoints[i * 4 + 3] = (rect.height() - (dbValue * 2 - 10)).toFloat()
            }
        }
        canvas.drawLines(mFFTPoints, paint)
    }
}