package com.droidcode.apps.kryptografia_projekt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    modifier: Modifier,
    navigateToEncryptScreen: () -> Unit,
    navigateToDecipherScreen: () -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(36.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.encrypt_app),
            style = MaterialTheme.typography.headlineLarge
        )
    }
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navigateToEncryptScreen() }, modifier.padding(8.dp)) {
            Text(stringResource(R.string.encrypt), style = MaterialTheme.typography.titleLarge)
        }

        Button(onClick = { navigateToDecipherScreen() }, modifier.padding(8.dp)) {
            Text(stringResource(R.string.decipher), style = MaterialTheme.typography.titleLarge)
        }
    }
}
