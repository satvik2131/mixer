package com.mixer.views

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mixer.viewmodel.UriHandlerVM

@Composable
fun MusicFilePicker(navController: NavController, viewModel: UriHandlerVM ) {
    val selectedMusicUri by viewModel.selectedMusicUri.observeAsState()

    // Create a launcher for the file picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Handle the result (Uri) here
            viewModel.setSelectedMusicUri(uri)
            navController.navigate("player")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            // Launch the file picker and specify MIME type for audio files
            launcher.launch("audio/*")

        }) {
            Text("Select Music File")
        }
    }
}

@Preview
@Composable
fun MusicFilePickerPreview() {
//    MusicFilePicker()
}
