package com.mixer.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder.AudioSource
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.mixer.utils.Mixer
import com.mixer.viewmodel.MusicFilePickerVM
import com.mixer.viewmodel.PlayerRecorderVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer


@Composable
fun PlayerAndRecorder(
    navController: NavController,
    backgroundMusicVM: MusicFilePickerVM,
    playerRecorderVM: PlayerRecorderVM
) {
    val context = LocalContext.current
    //Bg & mic audio
    val backgroundMusicUri by backgroundMusicVM.selectedMusicUri.observeAsState()
//    val bgMusicByteArray: ByteArray =
//        backgroundMusicVM.fileFromContentUri(context, backgroundMusicUri!!).readBytes() //byte array

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
            mixAndPlayAudio(
                playerRecorderVM = playerRecorderVM,
                context = context,
                backgroundMusicUri
            )
        } else {

        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun mixAndPlayAudio(
    playerRecorderVM: PlayerRecorderVM,
    context: Context,
    backgroundMusicUri: Uri?
) {
    val coroutineScope = rememberCoroutineScope()
    val sampleRate = 44100
    var uriAudioByteArr: ByteArray? = null

    //standard constants
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    //Mic Audio buffer size
    val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        channelConfig,
        audioFormat
    )

    val micBuffer = ByteArray(bufferSize)
    val audioTrack = AudioTrack(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build(),
        AudioFormat.Builder()
            .setEncoding(audioFormat)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build(),
        bufferSize,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )

    DisposableEffect(Dispatchers.IO) {
        coroutineScope.launch {
            //Background music buffer
            backgroundMusicUri?.apply {
                val micAudioRecord = playerRecorderVM.startMicAudioRecording(context)
                val micRecordRead = micAudioRecord?.read(micBuffer, 0, micBuffer.size)
                val mixer: Mixer = Mixer(context)
                audioTrack.play()
                while (micRecordRead != null && micRecordRead > 0) {
                    audioTrack.write(micBuffer, 0, micRecordRead)
//                    val mixedAudio = mixer.mixAudioStreams(micBuffer, backgroundMusicUri)
//                    uriAudioBuffer?.apply {
////                        val shorts = ShortArray(uriAudioBuffer!!.array().size / 2)
//                        playerRecorderVM.playMixedAudio(mixedAudio, 44100)
//                    }
                }
            }
        }

        onDispose {
            playerRecorderVM.stopRecording()
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun AudioRecorderAndPlayerPreview() {
//    AudioRecorderAndPlayer()
//}
