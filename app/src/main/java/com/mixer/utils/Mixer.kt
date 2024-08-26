package com.mixer.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import androidx.media3.common.audio.AudioMixingUtil
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.flow.Flow
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import kotlin.Short.Companion.MAX_VALUE

@UnstableApi
class Mixer(val context:Context) {
    fun mixAudioStreams(micAudio: ByteArray, bgAudio: Uri):ByteArray {
        return micAudio
//        val file1 = writeByteArrayToPCMFile(micAudio,"micAudio.pcm",context)
//        val file2 = writeByteArrayToPCMFile(bgAudio, "input2.pcm", context)
//        val cacheDir = context.cacheDir
//        val outputFile = File(cacheDir, "output.pcm")
//
//        if (!outputFile.parentFile.exists()) {
//            outputFile.parentFile.mkdirs()
//        }
//
//        if (!outputFile.exists()) {
//            outputFile.createNewFile()
//        }
//
//
//        val ffmpegCommand = arrayOf(
//            "-y",                          // Overwrite output files
//            "-f", "s16le",                 // Specify input format as PCM signed 16-bit little-endian
//            "-ar", "44100",                // Set the sample rate
//            "-ac", "2",                    // Set the number of audio channels
//            "-i", file1.absolutePath,      // Input file 1
//            "-f", "s16le",                 // Specify input format as PCM signed 16-bit little-endian
//            "-ar", "44100",                // Set the sample rate
//            "-ac", "2",                    // Set the number of audio channels
//            "-i", file2.absolutePath,      // Input file 2
//            "-filter_complex", "amix=inputs=2:duration=first:dropout_transition=2", // Mix the two inputs
//            outputFile.absolutePath        // Output file
//        )
//
//        FFmpegKit.executeAsync(ffmpegCommand.joinToString(" ")) { session ->
//            if (ReturnCode.isSuccess(session.returnCode)) {
//                // Success: Read the output file back into a ByteArray
//                val mixedAudioByteArray = outputFile.readBytes()
//                // Do something with mixedAudioByteArray
//            } else {
//                // Handle the error
//                Log.e("FFmpeg", "Error mixing audio: ${session.failStackTrace}")
//            }
//        }
//
//        val mixedAudioByteArray = outputFile.readBytes()
//        return mixedAudioByteArray
    }


    fun writeByteArrayToPCMFile(byteArray: ByteArray, fileName: String, context: Context): File {
        val file = File(context.cacheDir, fileName)
        file.outputStream().use { it.write(byteArray) }
        return file
    }


}