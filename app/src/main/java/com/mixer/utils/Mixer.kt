package com.mixer.utils

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import androidx.media3.common.audio.AudioMixingUtil
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.Flow

@UnstableApi
class Mixer {

    fun mixAudioStreams(stream1: ByteArray, stream2: ByteArray):ByteArray {
        val mixedStream = ByteArray(stream1.size)
        for (i in stream1.indices) {
            val sample1 = stream1[i].toInt()
            val sample2 = stream2[i].toInt()
            val mixedSample = (sample1 + sample2).coerceIn(Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt()).toByte()
            mixedStream[i] = mixedSample
        }

        return mixedStream
    }




}