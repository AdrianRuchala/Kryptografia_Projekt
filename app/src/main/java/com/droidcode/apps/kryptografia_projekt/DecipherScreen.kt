package com.droidcode.apps.kryptografia_projekt

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DecipherScreen(modifier: Modifier, viewModel: MainScreenViewModel, onNavigateBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var keyText by remember { mutableStateOf("") }
    var decipherType by remember { mutableStateOf(EncryptType.Polyalphabetic) }
    var decipherTypeText by remember { mutableStateOf("Polialfabetyczne") }
    val showAlertDialog = remember { mutableStateOf(false) }
    val decipheredText by remember { mutableStateOf(viewModel.decipheredText) }

    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            readFileContent(uri, context) { readText ->
                inputText = readText
            }
        }
    }

    if (showAlertDialog.value) {
        SelectEncryptionType(
            modifier,
            showAlertDialog
        ) { selectedDecipherType, selectedDecipherText ->
            decipherType = selectedDecipherType
            decipherTypeText = selectedDecipherText
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(all = 8.dp)
    ) {
        DecipherTopBar(modifier) { onNavigateBack() }

        Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(id = R.string.select_encryption_type),
                modifier = modifier,
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier.clickable { showAlertDialog.value = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = decipherTypeText)
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }

        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            label = {
                Text(stringResource(R.string.type_text))
            }
        )

        if (decipherType != EncryptType.Transposition) {
            TextField(
                value = keyText,
                onValueChange = { keyText = it },
                modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = {
                    Text(stringResource(R.string.type_key))
                },
                isError = keyText.isEmpty()
            )
        }

        Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    filePicker.launch(arrayOf("text/plain"))
                }
            ) {
                Text(stringResource(R.string.read_from_file))
            }

            Button(
                onClick = {
                    if (decipherType != EncryptType.Transposition) {
                        if (keyText.isNotEmpty()) {
                            viewModel.decipherText(inputText, keyText, decipherType)
                        }
                    } else {
                        viewModel.decipherText(inputText, keyText, decipherType)
                    }
                },
            ) {
                Text(stringResource(R.string.decipher))
            }
        }


        Spacer(modifier = modifier.padding(4.dp))
        Text(stringResource(R.string.deciphered_text), style = MaterialTheme.typography.titleMedium)
        Text(text = decipheredText.value)
    }
}

@Composable
fun DecipherTopBar(modifier: Modifier, onNavigateBack: () -> Unit) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            modifier
                .size(36.dp)
                .clickable { onNavigateBack() }
        )

        Text(stringResource(R.string.decipher), style = MaterialTheme.typography.titleMedium)

        Spacer(modifier.size(36.dp))
    }

    HorizontalDivider(modifier.padding(vertical = 8.dp))
}

private fun readFileContent(uri: Uri, context: Context, onSuccess: (String) -> Unit) {
    try {
        val selectedFile = context.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(selectedFile))
        val readText = reader.readText()
        onSuccess(readText)
    } catch (e: Exception) {
        Log.d(TAG, e.message.toString())
    }
}
