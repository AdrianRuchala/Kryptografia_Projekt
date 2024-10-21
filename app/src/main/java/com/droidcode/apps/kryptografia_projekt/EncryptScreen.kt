package com.droidcode.apps.kryptografia_projekt

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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

@Composable
fun EncryptScreen(modifier: Modifier, viewModel: MainScreenViewModel, onNavigateBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var keyText by remember { mutableStateOf("") }
    var encryptionType by remember { mutableStateOf(EncryptType.Polyalphabetic) }
    var encryptionTypeText by remember { mutableStateOf("Polialfabetyczne") }
    val showAlertDialog = remember { mutableStateOf(false) }
    val encryptedText by remember { mutableStateOf(viewModel.encryptedText) }

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
        ) { selectedEncryptionType, selectedEncryptionText ->
            encryptionType = selectedEncryptionType
            encryptionTypeText = selectedEncryptionText
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(all = 8.dp)
    ) {
        TopBar(modifier) { onNavigateBack() }

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
                Text(text = encryptionTypeText)
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

        if (encryptionType == EncryptType.Polyalphabetic) {
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
                    filePicker.launch(arrayOf("text/plain")) //wyświetlą się tylko pliki tekstowe
                }
            ) {
                Text(stringResource(R.string.select_file))
            }

            Button(
                onClick = {
                    if (encryptionType == EncryptType.Polyalphabetic) {
                        if (keyText.isNotEmpty()) {
                            viewModel.encryptText(inputText, keyText, encryptionType)
                        }
                    } else {
                        viewModel.encryptText(inputText, keyText, encryptionType)
                    }

                },
            ) {
                Text(stringResource(R.string.encrypt))
            }
        }


        Spacer(modifier = modifier.padding(4.dp))
        Text(stringResource(R.string.encrypted_text), style = MaterialTheme.typography.titleMedium)
        Text(text = encryptedText.value)
    }
}

@Composable
fun TopBar(modifier: Modifier, onNavigateBack: () -> Unit){
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

        Text(stringResource(R.string.encrypt), style = MaterialTheme.typography.titleMedium)

        Spacer(modifier.size(36.dp))
    }

    HorizontalDivider(modifier.padding(vertical = 8.dp))
}

@Composable
fun SelectEncryptionType(
    modifier: Modifier,
    showAlertDialog: MutableState<Boolean>,
    onDismiss: (EncryptType, String) -> Unit
) {
    val encryptTypes = arrayOf(
        "Polialfabetyczne",
        "Przestawieniowe"
    )
    AlertDialog(onDismissRequest = { showAlertDialog.value = false },
        title = { Text(stringResource(R.string.select_encryption)) },
        text = {
            Column(modifier.fillMaxWidth()) {
                encryptTypes.forEach { encryptType ->
                    Text(
                        text = encryptType,
                        modifier = modifier
                            .fillMaxWidth()
                            .clickable {
                                when (encryptType) {
                                    encryptTypes[0] -> {
                                        onDismiss(EncryptType.Polyalphabetic, encryptType)
                                    }

                                    encryptTypes[1] -> {
                                        onDismiss(EncryptType.Transposition, encryptType)
                                    }

                                }
                                showAlertDialog.value = false
                            }
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showAlertDialog.value = false
                }
            ) {
                Text(stringResource(R.string.dialog_negative))
            }
        }
    )
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
