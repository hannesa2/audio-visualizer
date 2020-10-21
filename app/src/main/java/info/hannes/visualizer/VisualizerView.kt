package info.hannes.visualizer

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.util.AttributeSet
import android.view.View
import info.hannes.visualizer.data.AudioData
import info.hannes.visualizer.data.FFTData
import info.hannes.visualizer.renderer.Renderer
import java.util.*

/**
 * A class that draws visualizations of data received from a
 * [Visualizer.OnDataCaptureListener.onWaveFormDataCapture] and
 * [Visualizer.OnDataCaptureListener.onFftDataCapture]
 */
class VisualizerView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : View(context, attrs) {
    private var mBytes: ByteArray?
    private var mFFTBytes: ByteArray?
    private val mRect = Rect()
    private var mVisualizer: Visualizer? = null
    private var audioData : AudioData? = null
    private var fftData : FFTData? = null
    private val localMatrix = Matrix()
    private var mRenderers: MutableSet<Renderer>? = null
    private val mFlashPaint = Paint()
    private val mFadePaint = Paint()

    init {
        mBytes = null
        mFFTBytes = null
        mFlashPaint.color = Color.argb(122, 255, 255, 255)
        mFadePaint.color = Color.argb(238, 255, 255, 255) // Adjust alpha to change how quickly the image fades
        mFadePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        mRenderers = HashSet()
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
        mVisualizer = Visualizer(player.audioSessionId)
        mVisualizer!!.captureSize = Visualizer.getCaptureSizeRange()[1]

        // Pass through Visualizer data to VisualizerView
        val captureListener: OnDataCaptureListener = object : OnDataCaptureListener {
            override fun onWaveFormDataCapture(visualizer: Visualizer, bytes: ByteArray, samplingRate: Int) {
                updateVisualizer(bytes)
            }

            override fun onFftDataCapture(visualizer: Visualizer, bytes: ByteArray, samplingRate: Int) {
                updateVisualizerFFT(bytes)
            }
        }
        mVisualizer!!.setDataCaptureListener(captureListener, Visualizer.getMaxCaptureRate() / 2, true, true)

        // Enabled Visualizer and disable when we're done with the stream
        mVisualizer!!.enabled = true
        player.setOnCompletionListener(OnCompletionListener { mediaPlayer: MediaPlayer? -> mVisualizer!!.enabled = false })
    }

    fun addRenderer(renderer: Renderer) {
            mRenderers!!.add(renderer)
    }

    fun clearRenderers() {
        mRenderers!!.clear()
    }

    /**
     * Call to release the resources used by VisualizerView. Like with the
     * MediaPlayer it is good practice to call this method
     */
    fun release() {
        mVisualizer!!.release()
    }

    /**
     * Pass data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * [Visualizer.OnDataCaptureListener.onWaveFormDataCapture]
     */
    fun updateVisualizer(bytes: ByteArray?) {
        mBytes = bytes
        audioData = AudioData(mBytes!!)

        invalidate()
    }

    /**
     * Pass FFT data to the visualizer. Typically this will be obtained from the
     * Android Visualizer.OnDataCaptureListener call back. See
     * [Visualizer.OnDataCaptureListener.onFftDataCapture]
     */
    fun updateVisualizerFFT(bytes: ByteArray?) {
        mFFTBytes = bytes

        fftData = FFTData(mFFTBytes!!)

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

        mBytes?.let {
            // Render all audio renderers
            audioData?.bytes = it
            for (renderer in mRenderers!!) {
                renderer.render(mCanvas!!, audioData!!, mRect)
            }
        }

        mFFTBytes?.let {
            // Render all FFT renderers
            fftData?.bytes = it
            for (renderer in mRenderers!!) {
                renderer.render(mCanvas!!, fftData!!, mRect)
            }
        }

        // Fade out old contents
        mCanvas!!.drawPaint(mFadePaint)
        if (mFlash) {
            mFlash = false
            mCanvas!!.drawPaint(mFlashPaint)
        }
        canvas.drawBitmap(mCanvasBitmap!!, localMatrix, null)
    }

}