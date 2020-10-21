package info.hannes.visualizer.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import info.hannes.visualizer.R
import info.hannes.visualizer.utils.SystemPropertiesProxy.getBoolean

object TunnelPlayerWorkaround {
    private const val TAG = "TunnelPlayerWorkaround"
    private const val SYSTEM_PROP_TUNNEL_DECODE_ENABLED = "tunnel.decode"

    /**
     * Obtain "tunnel.decode" system property value
     *
     * @param context Context
     * @return Whether tunnel player is enabled
     */
    fun isTunnelDecodeEnabled(context: Context?): Boolean {
        return getBoolean(context!!, SYSTEM_PROP_TUNNEL_DECODE_ENABLED, false)
    }

    /**
     * Create silent MediaPlayer instance to avoid tunnel player issue
     *
     * @param context Context
     * @return MediaPlayer instance
     */
    fun createSilentMediaPlayer(context: Context?): MediaPlayer? {
        var result = false
        var mp: MediaPlayer? = null
        try {
            mp = MediaPlayer.create(context, R.raw.workaround_1min)
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC)

            // NOTE: start() is no needed
            // mp.start();
            result = true
        } catch (e: RuntimeException) {
            Log.e(TAG, "createSilentMediaPlayer()", e)
        } finally {
            if (!result && mp != null) {
                try {
                    mp.release()
                } catch (e: IllegalStateException) {
                }
            }
        }
        return mp
    }
}