package info.hannes.visualizer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import info.hannes.visualizer.databinding.MainBinding
import nativ.hannes.info.visualizer.renderer.BarGraphRenderer
import nativ.hannes.info.visualizer.renderer.CircleBarRenderer
import nativ.hannes.info.visualizer.renderer.CircleRenderer
import nativ.hannes.info.visualizer.renderer.LineRenderer
import info.hannes.visualizer.utils.TunnelPlayerWorkaround


class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainBinding

    private var isAudioPermissionGranted = true
    private var mediaPlayer: MediaPlayer? = null
    private var silentPlayer /* to avoid tunnel player issue */: MediaPlayer? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkCameraPermissions()

        binding.buttonBarPressed.setOnClickListener { addBarGraphRenderers() }
        binding.buttonCirclePressed.setOnClickListener { addCircleRenderer() }
        binding.buttonCircleBarPressed.setOnClickListener { addCircleBarRenderer() }
        binding.buttonLinePressed.setOnClickListener { addLineRenderer() }
        binding.buttonClearPressed.setOnClickListener { binding.visualizerView.clearRenderers() }

        binding.buttonStopPressed.setOnClickListener { mediaPlayer?.stop() }
        binding.buttonStartPressed.setOnClickListener {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.prepare()
                mediaPlayer?.start()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initTunnelPlayerWorkaround()
    }

    override fun onPause() {
        cleanUp()
        super.onPause()
    }

    override fun onDestroy() {
        cleanUp()
        super.onDestroy()
    }

    private fun init() {
        mediaPlayer = MediaPlayer.create(this, R.raw.test)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        // We need to link the visualizer view to the media player so that it displays something
        binding.visualizerView.link(mediaPlayer)

        // Start with just line renderer
        addLineRenderer()
    }

    private fun cleanUp() {
        mediaPlayer?.let {
            binding.visualizerView.release()
            it.release()
            mediaPlayer = null
        }
        silentPlayer?.release()
        silentPlayer = null
    }

    // NOTE:
    //   This code is not required for visualizing default "test.mp3" file,
    //   because tunnel player is used when duration is longer than 1 minute.
    //   (default "test.mp3" file: 8 seconds)
    //
    private fun initTunnelPlayerWorkaround() {
        // Read "tunnel.decode" system property to determine
        // the workaround is needed
        if (TunnelPlayerWorkaround.isTunnelDecodeEnabled(this)) {
            silentPlayer = TunnelPlayerWorkaround.createSilentMediaPlayer(this)
        }
    }

    // Methods for adding renderers to visualizer
    private fun addBarGraphRenderers() {
        val paint = Paint()
        paint.strokeWidth = 50f
        paint.isAntiAlias = true
        paint.color = Color.argb(200, 56, 138, 252)
        val barGraphRendererBottom = BarGraphRenderer(16, paint, false)
        binding.visualizerView.addRenderer(barGraphRendererBottom)
        val paint2 = Paint()
        paint2.strokeWidth = 12f
        paint2.isAntiAlias = true
        paint2.color = Color.argb(200, 181, 111, 233)
        val barGraphRendererTop = BarGraphRenderer(4, paint2, true)
        binding.visualizerView.addRenderer(barGraphRendererTop)
    }

    private fun addCircleBarRenderer() {
        val paint = Paint()
        paint.strokeWidth = 8f
        paint.isAntiAlias = true
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
        paint.color = Color.argb(255, 222, 92, 143)
        val circleBarRenderer = CircleBarRenderer(paint, 32, true)
        binding.visualizerView.addRenderer(circleBarRenderer)
    }

    private fun addCircleRenderer() {
        val paint = Paint()
        paint.strokeWidth = 3f
        paint.isAntiAlias = true
        paint.color = Color.argb(255, 222, 92, 143)
        val circleRenderer = CircleRenderer(paint, true)
        binding.visualizerView.addRenderer(circleRenderer)
    }

    private fun addLineRenderer() {
        val linePaint = Paint()
        linePaint.strokeWidth = 1f
        linePaint.isAntiAlias = true
        linePaint.color = Color.argb(88, 0, 128, 255)
        val lineFlashPaint = Paint()
        lineFlashPaint.strokeWidth = 5f
        lineFlashPaint.isAntiAlias = true
        lineFlashPaint.color = Color.argb(188, 255, 255, 255)
        val lineRenderer = LineRenderer(linePaint, lineFlashPaint, true)
        binding.visualizerView.addRenderer(lineRenderer)
    }

    private fun checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            isAudioPermissionGranted = false
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Enable audio permission from settings", Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_AUDIO)
            }
        } else {
            if (isAudioPermissionGranted) {
                init()
            } else {
                isAudioPermissionGranted = true
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_AUDIO -> onRequestAudio(grantResults)
            else -> Unit
        }
    }

    private fun onRequestAudio(grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            init()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied_audio_toast), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_AUDIO = 101
    }
}