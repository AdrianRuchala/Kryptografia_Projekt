package com.droidcode.apps.kryptografia_projekt

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MainScreen(modifier: Modifier) {
    Scaffold { padding ->
        View(modifier.padding(padding))
    }
}

@Composable
fun View(modifier: Modifier){
    Text(text = "Hello World!", modifier)
}
