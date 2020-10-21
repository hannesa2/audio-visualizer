package nativ.hannes.info.visualizer

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.util.AttributeSet
import android.view.View
import nativ.hannes.info.visualizer.data.AudioData
import nativ.hannes.info.visualizer.data.FFTData
import nativ.hannes.info.visualizer.renderer.Renderer
import java.util.*

/**
 * A class that draws visualizations of data received from a
 * [Visualizer.OnDataCaptureListener.onWaveFormDataCapture] and
 * [Visualizer.OnDataCaptureListener.onFftDataCapture]
 */
class VisualizerView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : View(context, attrs) {
    private var bytes: ByteArray?
    private var fftBytes: ByteArray?
    private val mRect = Rect()
    private var visualizer: Visualizer? = null
    private var audioData: AudioData? = null
    private var fftData: FFTData? = null
    private val localMatrix = Matrix()
    private var renderers: MutableSet<Renderer>? = null
    private val flashPaint = Paint()
    private val fadePaint = Paint()

    init {
        bytes = null
        fftBytes = null
        flashPaint.color = Color.argb(122, 255, 255, 255)
        fadePaint.color = Color.argb(238, 255, 255, 255) // Adjust alpha to change how quickly the image fades
        fadePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        renderers = HashSet()
    }

    /**
     * Links the visualizer to a player
     *
     * @param player - MediaPlayer instance to link to
     */
    fun link(player: MediaPlayer?) {
        if (player == null) {
            throw NullPointerException("Cannot link to null MediaPlayer")
        }

        // Create the Visualizer object and attach it to our media player.
        visualizer = Visualizer(player.audioSessionId)
        visualizer!!.captureSize = Visualizer.getCaptureSizeRange()[1]

        // Pass through Visualizer data to VisualizerView
        val captureListener: OnDataCaptureListener = object : OnDataCaptureListener {
            override fun onWaveFormDataCapture(updateVisualizer: Visualizer, updateBytes: ByteArray, samplingRate: Int) {
                updateVisualizer(updateBytes)
            }

            override fun onFftDataCapture(updateVisualizer: Visualizer, updateBytes: ByteArray, samplingRate: Int) {
                updateVisualizerFFT(updateBytes)
            }
        }
        visualizer!!.setDataCaptureListener(captureListener, Visualizer.getMaxCaptureRate() / 2, true, true)

        // Enabled Visualizer and disable when we're done with the stream
        visualizer!!.enabled = true
        player.setOnCompletionListener(OnCompletionListener { mediaPlayer: MediaPlayer? -> visualizer!!.enabled = false })
    }

    fun addRenderer(renderer: Renderer) {
        renderers!!.add(renderer)
    }

    fun clearRenderers() {
        renderers!!.clear()
    }

    /**
     * Call to release the resources used by VisualizerView. Like with the
     * MediaPlayer it is good practice to call this method
     */
    fun release() {
        visualizer!!.release()
    }

    /**
     * Pass data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * [Visualizer.OnDataCaptureListener.onWaveFormDataCapture]
     */
    fun updateVisualizer(updateBytes: ByteArray?) {
        bytes = updateBytes
        audioData = AudioData(bytes!!)

        invalidate()
    }

    /**
     * Pass FFT data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * [Visualizer.OnDataCaptureListener.onFftDataCapture]
     */
    fun updateVisualizerFFT(updateBytes: ByteArray?) {
        fftBytes = updateBytes

        fftData = FFTData(fftBytes!!)

        invalidate()
    }

    var mFlash = false

    /**
     * Call this to make the visualizer flash. Useful for flashing at the start
     * of a song/loop etc...
     */
    fun flash() {
        mFlash = true
        invalidate()
    }

    var mCanvasBitmap: Bitmap? = null
    var mCanvas: Canvas? = null
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Create canvas once we're ready to draw
        mRect[0, 0, width] = height
        if (mCanvasBitmap == null) {
            mCanvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        if (mCanvas == null) {
            mCanvas = Canvas(mCanvasBitmap!!)
        }

        bytes?.let {
            // Render all audio renderers
            audioData?.bytes = it
            for (renderer in renderers!!) {
                renderer.render(mCanvas!!, audioData!!, mRect)
            }
        }

        fftBytes?.let {
            // Render all FFT renderers
            fftData?.bytes = it
            for (renderer in renderers!!) {
                renderer.render(mCanvas!!, fftData!!, mRect)
            }
        }

        // Fade out old contents
        mCanvas!!.drawPaint(fadePaint)
        if (mFlash) {
            mFlash = false
            mCanvas!!.drawPaint(flashPaint)
        }
        canvas.drawBitmap(mCanvasBitmap!!, localMatrix, null)
    }

}