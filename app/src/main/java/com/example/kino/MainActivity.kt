package com.example.kino

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.kino.screens.CinemaApp
import com.example.kino.ui.theme.KinoTheme
import com.example.kino.viewmodel.CinemaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: CinemaViewModel by viewModels()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            KinoTheme(darkTheme = isDarkTheme) {
                CinemaApp(viewModel)
            }
        }
    }
}