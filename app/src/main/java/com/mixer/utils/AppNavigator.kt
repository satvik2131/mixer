package com.mixer.utils

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mixer.viewmodel.UriHandlerVM
import com.mixer.views.MusicFilePicker
import com.mixer.views.PlayerAndRecorder

@Composable
fun AppNavigator() {
    val navController = rememberNavController();
    val uriViewModel = UriHandlerVM()
    NavHost(navController = navController, startDestination = "takemusic") {
        composable ("takemusic"){ MusicFilePicker(navController,uriViewModel) }
        composable("player") { PlayerAndRecorder(navController, uriViewModel) }
    }
}