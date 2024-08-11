package com.mixer.views

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder.AudioSource
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.mixer.viewmodel.MusicFilePickerVM
import com.mixer.viewmodel.PlayerRecorderVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun PlayerAndRecorder(
    navController: NavController,
    backgroundMusicVM: MusicFilePickerVM,
    playerRecorderVM: PlayerRecorderVM
) {
    val context = LocalContext.current
    val micAudio by playerRecorderVM.micAudio.observeAsState()
    val backgroundMusic by backgroundMusicVM.selectedMusicUri.observeAsState()

    var isRecording by remember { mutableStateOf(false) }
    //Permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isRecording = true
            } else {
                //permission denied
                Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
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
//            StartRecordingAndPlaying()
        } else {

        }
    }
}


@SuppressLint("MissingPermission")
@Composable
fun StartRecordingAndPlaying() {
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    var bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

    val audioRecord = remember {
        AudioRecord(
            AudioSource.VOICE_COMMUNICATION,
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
