package com.mixer.views

import android.Manifest
import android.content.Context
import android.media.*
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.mixer.viewmodel.MusicFilePickerVM
import com.mixer.viewmodel.PlayerRecorderVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Composable
fun PlayerAndRecorder(
    navController: NavController,
    backgroundMusicVM: MusicFilePickerVM,
    playerRecorderVM: PlayerRecorderVM
) {
    val context = LocalContext.current
    val backgroundMusicUri by backgroundMusicVM.selectedMusicUri.observeAsState()

    var isRecording by remember { mutableStateOf(false) }

    // Permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isRecording = true
            } else {
                // Permission denied
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
            mixAndPlayAndSaveAudio(
                playerRecorderVM = playerRecorderVM,
                context = context,
                backgroundMusicUri = backgroundMusicUri,
                isRecording = isRecording
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun mixAndPlayAndSaveAudio(
    playerRecorderVM: PlayerRecorderVM,
    context: Context,
    backgroundMusicUri: Uri?,
    isRecording: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val sampleRate = 44100

    // Standard constants
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    // Mic Audio buffer size
    val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        channelConfig,
        audioFormat
    )

    val micBuffer = ByteArray(bufferSize)
    val audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC,
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize,
        AudioTrack.MODE_STREAM
    )

    // Create a file to save the mixed audio
    val outputFile = File(context.filesDir, "mixed_audio.pcm")
    val fileOutputStream = FileOutputStream(outputFile)

    // Extract MP3 audio data
    val mp3AudioData = backgroundMusicUri?.let { extractMp3AudioData(context, it) }

    DisposableEffect(Dispatchers.IO) {
        coroutineScope.launch {
            val micAudioRecord = playerRecorderVM.startMicAudioRecording(context)
            audioTrack.play()

            while (isRecording) {
                val micRecordRead = micAudioRecord?.read(micBuffer, 0, micBuffer.size)

                if (micRecordRead != null && micRecordRead > 0) {
                    // Mix the microphone audio with the MP3 audio data
                    val mixedAudio = mixAudioStreams(micBuffer, mp3AudioData, micRecordRead)

                    // Play the mixed audio
                    audioTrack.write(mixedAudio, 0, mixedAudio.size)

                    // Save the mixed audio to a file
                    try {
                        fileOutputStream.write(mixedAudio)
                    } catch (e: IOException) {
                        Log.e("MixAndSaveAudio", "Error writing to file: ${e.message}")
                    }
                }
            }

            audioTrack.stop()
            audioTrack.release()

            // Ensure the file output stream is closed
            try {
                fileOutputStream.close()
            } catch (e: IOException) {
                Log.e("MixAndSaveAudio", "Error closing file output stream: ${e.message}")
            }
        }

        onDispose {
            playerRecorderVM.stopRecording()
        }
    }
}

fun extractMp3AudioData(context: Context, uri: Uri): ByteArray? {
    val mediaPlayer = MediaPlayer().apply {
        setDataSource(context, uri)
        prepare()
    }

    val durationInMs = mediaPlayer.duration

    // Log the duration for debugging
    Log.d("MP3", "Duration in ms: $durationInMs")

    if (durationInMs <= 0) {
        Log.e("MP3", "Invalid duration: $durationInMs ms")
        return null
    }

    // Assume a standard sample rate and channels for calculation
    val sampleRate = 44100  // 44.1kHz sample rate
    val numChannels = 2  // Assuming stereo audio
    val bytesPerSample = 2  // 16-bit PCM (2 bytes per sample)

    // Calculate buffer size safely
    val bufferSize = (durationInMs / 1000.0 * sampleRate * numChannels * bytesPerSample).toInt()

    // Log the calculated buffer size
    Log.d("MP3", "Calculated buffer size: $bufferSize bytes")

    if (bufferSize <= 0) {
        Log.e("MP3", "Invalid buffer size calculated: $bufferSize bytes")
        return null
    }

    val buffer = ByteArray(bufferSize)

    // Here you'd fill the buffer with the actual MP3 data. This depends on your approach.
    // For now, we just return the empty buffer as a placeholder.

    mediaPlayer.start()

    mediaPlayer.setOnCompletionListener {
        mediaPlayer.release()
    }

    return buffer
}



fun mixAudioStreams(micBuffer: ByteArray, mp3AudioData: ByteArray?, micRecordRead: Int): ByteArray {
    if (mp3AudioData == null) return micBuffer

    val mixedAudio = ByteArray(micRecordRead)

    for (i in 0 until micRecordRead step 2) {
        val micSample = ByteBuffer.wrap(micBuffer, i, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt()
        val mp3Sample = if (i < mp3AudioData.size) {
            ByteBuffer.wrap(mp3AudioData, i, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt()
        } else {
            0
        }

        val mixedSample = (micSample + mp3Sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        ByteBuffer.wrap(mixedAudio, i, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(mixedSample.toShort())
    }

    return mixedAudio
}
