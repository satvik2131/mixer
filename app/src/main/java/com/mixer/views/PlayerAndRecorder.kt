package com.mixer.views

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toFile
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mixer.R
import com.mixer.record.Recorder
import com.mixer.utils.Mixer
import com.mixer.viewmodel.UriHandlerVM
import java.io.File
import java.io.OutputStream
import java.net.URI

@OptIn(UnstableApi::class)
@Composable
fun PlayerAndRecorder(navController: NavController, viewModel: UriHandlerVM) {
    val selectedMusicUri by viewModel.selectedMusicUri.observeAsState()
    val (isStarted, setIsStarted) = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var micOp:File? = null
    val recorder by lazy {
        Recorder(context = context)
    }

    LaunchedEffect(key1 = isStarted) {
        val mixer:Mixer = Mixer()
        if(micOp!=null && selectedMusicUri!=null){
            val micStream:ByteArray = micOp!!.readBytes()
            val music:ByteArray = viewModel.fileFromContentUri(context, selectedMusicUri!!).readBytes()
            val output = mixer.mixAudioStreams(micStream,music)
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_OUT_STEREO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT

            playAudioFromByteArray(output, sampleRate, channelConfig, audioFormat)//            Log.d("MixedMusic",output.toString())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isStarted) {
            //starts recording
            File(context.cacheDir,"audio.mp3").also {
                recorder.start(it)
                micOp = it
            }
            IconButton(onClick = {
                recorder.stop();
                setIsStarted(false)
            }) {
                Icon(imageVector = Icons.Rounded.Close, "something")
            }
//            Player(selectedMusicUri!!)
        } else {
            IconButton(onClick = { setIsStarted(true) }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_mic_24),
                    contentDescription = "start singing",
                )
            }
            Text("Start Singing")
        }
    }
}

fun playAudioFromByteArray(audioData: ByteArray, sampleRate: Int, channelConfig: Int, audioFormat: Int) {
    val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    val audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC,
        sampleRate,
        channelConfig,
        audioFormat,
        bufferSize,
        AudioTrack.MODE_STREAM
    )

    audioTrack.play()
    audioTrack.write(audioData, 0, audioData.size)
    audioTrack.stop()
    audioTrack.release()
}

@Composable
fun Player(uri: Uri) {
    val context = LocalContext.current
    val player = ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(uri))
    }

    val playerView = PlayerView(context)
    val playWhenReady by rememberSaveable {
        mutableStateOf(true)
    }
    playerView.player = player
    LaunchedEffect(player) {
        player.prepare()
        player.playWhenReady = playWhenReady
    }
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = { playerView })
}

@Preview(showBackground = true, backgroundColor = 0xFFFF)
@Composable
fun PlayerAndRecorderPreview() {
    val navController: NavController = rememberNavController()
    val viewModel: UriHandlerVM = UriHandlerVM();

    PlayerAndRecorder(navController, viewModel)
}
