package nativ.hannes.info.visualizer.renderer

import android.graphics.Canvas
import android.graphics.Rect
import nativ.hannes.info.visualizer.data.AudioData
import nativ.hannes.info.visualizer.data.FFTData

abstract class Renderer {
    // Have these as members, so we don't have to re-create them each time
    protected var points: FloatArray = FloatArray(0)
    protected var fftPoints: FloatArray = FloatArray(0)
    // As the display of raw/FFT audio will usually look different, subclasses
    // will typically only implement one of the below methods
    /**
     * Implement this method to render the audio data onto the canvas
     *
     * @param canvas - Canvas to draw on
     * @param data   - Data to render
     * @param rect   - Rect to render into
     */
    abstract fun onRender(canvas: Canvas, data: AudioData, rect: Rect)

    /**
     * Implement this method to render the FFT audio data onto the canvas
     *
     * @param canvas - Canvas to draw on
     * @param data   - Data to render
     * @param rect   - Rect to render into
     */
    abstract fun onRender(canvas: Canvas, data: FFTData, rect: Rect)
    // These methods should actually be called for rendering
    /**
     * Render the audio data onto the canvas
     *
     * @param canvas - Canvas to draw on
     * @param data   - Data to render
     * @param rect   - Rect to render into
     */
    fun render(canvas: Canvas, data: AudioData, rect: Rect) {
        if (points.size < data.bytes.size * 4) {
            points = FloatArray(data.bytes.size * 4)
        }
        onRender(canvas, data, rect)
    }

    /**
     * Render the FFT data onto the canvas
     *
     * @param canvas - Canvas to draw on
     * @param data   - Data to render
     * @param rect   - Rect to render into
     */
    fun render(canvas: Canvas, data: FFTData, rect: Rect) {
        if (fftPoints.size < data.bytes.size * 4) {
            fftPoints = FloatArray(data.bytes.size * 4)
        }
        onRender(canvas, data, rect)
    }
}