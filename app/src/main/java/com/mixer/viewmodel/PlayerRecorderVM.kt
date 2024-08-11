package com.mixer.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder.AudioSource
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class PlayerRecorderVM @Inject constructor() : ViewModel() {
    private val _micAudio = MutableLiveData<ByteArray>(null)
    val micAudio: LiveData<ByteArray> get() = _micAudio

    fun startMicAudioRecording(context: Context) {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        var bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

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
            return

        } else {
            val audioRecord = AudioRecord(
                AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM
            )

            audioRecord.startRecording()
            val audioBuffer: ByteArray = ByteArray(bufferSize)
            while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = audioRecord.read(audioBuffer, 0, bufferSize);
                if (read > 0) {

                }
            }
        }
    }
}