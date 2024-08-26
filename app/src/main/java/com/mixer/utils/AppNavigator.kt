package com.mixer.utils

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mixer.viewmodel.MusicFilePickerVM
import com.mixer.viewmodel.PlayerRecorderVM
import com.mixer.views.MusicFilePicker
import com.mixer.views.PlayerAndRecorder

@Composable
fun AppNavigator() {
    val navController = rememberNavController();
    val backgroundMusicVM = MusicFilePickerVM()
    val playerRecorderVm = PlayerRecorderVM()
    NavHost(navController = navController, startDestination = "takemusic") {
        composable ("takemusic"){ MusicFilePicker(navController,backgroundMusicVM) }
        composable("player") { PlayerAndRecorder(navController, backgroundMusicVM,playerRecorderVm) }
    }
}