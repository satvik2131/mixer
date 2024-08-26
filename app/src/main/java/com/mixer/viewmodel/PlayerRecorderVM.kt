package com.mixer.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.media.audiofx.NoiseSuppressor
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    // Start recording from the microphone
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
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            ).apply {
                startRecording()
            }

            // Apply noise suppression to improve mic input clarity
            NoiseSuppressor.create(audioRecord!!.audioSessionId)

            return audioRecord
        }
    }

    // Play mixed audio through the speaker
    fun playMixedAudio(mixedAudio: ByteArray, sampleRate: Int = 44100) {
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            mixedAudio.size,
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

    // Extract audio data from the selected MP3 file
    suspend fun extractAudioData(context: Context, uri: Uri): ByteBuffer = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        if (extractor.trackCount <= 0) {
            throw IllegalArgumentException("No audio track found in the provided URI")
        }

        val format: MediaFormat = extractor.getTrackFormat(0)

        val bufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
        val buffer = ByteBuffer.allocate(bufferSize)

        extractor.selectTrack(0)

        while (true) {
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break
            extractor.advance()
        }

        extractor.release()
        buffer
    }

    // Mix microphone and MP3 audio streams
    fun mixAudioStreams(micAudio: ByteArray, mp3Audio: ByteBuffer): ByteArray {
        // Mix mic audio with MP3 audio
        val mixedAudio = ByteArray(micAudio.size)
        for (i in mixedAudio.indices) {
            mixedAudio[i] = (micAudio[i] + mp3Audio.get(i)).toByte()
        }
        return mixedAudio
    }
}
