package com.mixer.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.mixer.viewmodel.UriHandlerVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun PlayerAndRecorder(navController: NavController,viewModel:UriHandlerVM) {
    val context = LocalContext.current

    var isRecording by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isRecording = true
            } else {
                // Handle permission denied
            }
        }
    )

    Column {
        Button(onClick = {
            if (isRecording) {
                isRecording = false
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }) {
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }

        if (isRecording) {
            StartRecordingAndPlaying()
        }
    }
}


@SuppressLint("MissingPermission")
@Composable
fun StartRecordingAndPlaying() {
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2
    val audioRecord = remember {
        AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )
    }


    val audioTrack = remember {
        AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
    }

    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            audioRecord.startRecording()
            audioTrack.play()
            val audioBuffer = ByteArray(bufferSize)
            while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = audioRecord.read(audioBuffer, 0, bufferSize)
                if (read > 0) {
                    audioTrack.write(audioBuffer, 0, read)
                }
            }
            audioTrack.stop()
            audioTrack.release()
        }

        onDispose {
            if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop()
            }
            audioRecord.release()

            if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop()
            }
            audioTrack.release()
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun AudioRecorderAndPlayerPreview() {
//    AudioRecorderAndPlayer()
//}
