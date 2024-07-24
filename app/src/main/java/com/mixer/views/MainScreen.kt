package com.mixer.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mixer.utils.AppNavigator

@Composable
fun MainScreen(paddingValues: PaddingValues){
    Box(modifier = Modifier.padding(paddingValues).fillMaxSize()){
        AppNavigator()
    }
}