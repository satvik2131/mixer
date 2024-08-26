package com.mixer.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaRecorder.AudioSource
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.C.PcmEncoding
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.DefaultAudioMixer
import java.nio.ByteBuffer
import javax.inject.Inject

class PlayerRecorderVM @Inject constructor() : ViewModel() {

    //playing and recording
    private var audioTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null

    //standard constants
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    var bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2


    fun startMicAudioRecording(context: Context): AudioRecord? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                context,
                "Please enable recording permissions from settings",
                Toast.LENGTH_SHORT
            ).show()
            return null

        } else {
            //Audio->Record
            audioRecord = AudioRecord(
                AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            ).apply {
                startRecording()
            }

            return audioRecord
        }
    }


    fun playMixedAudio(mixedAudio: ByteArray, sampleRate: Int = 44100) {
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            mixedAudio.size /2,  // Size in bytes
            AudioTrack.MODE_STATIC
        ).apply {
            write(mixedAudio, 0, mixedAudio.size)
            play()
        }
    }

    fun stopRecording() {
        audioRecord?.stop()
        audioTrack?.stop()
        audioRecord?.release()
        audioTrack?.release()
    }


    fun extractAudioData(context: Context, uri: Uri): ByteBuffer {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        // Ensure the track is selected correctly
        if (extractor.trackCount <= 0) {
            throw IllegalArgumentException("No audio track found in the provided URI")
        }

        val format: MediaFormat = extractor.getTrackFormat(0)

        val mime = format.getString(MediaFormat.KEY_MIME)
            ?: throw IllegalArgumentException("No MIME type found")

        // Check for the key existence before accessing
        val bufferSize = if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        } else {
            1024 * 1024  // Default buffer size
        }

        val buffer = ByteBuffer.allocate(bufferSize)

        extractor.selectTrack(0)

        while (true) {
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break
            extractor.advance()
        }

        extractor.release()
        return buffer
    }

}