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
import androidx.compose.foundation.lazy.LazyColumn
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
fun DecipherScreen(modifier: Modifier, viewModel: DecryptViewModel, onNavigateBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var keyText by remember { mutableStateOf("") }
    var publicKeyText by remember { mutableStateOf("") }
    var decryptType by remember { mutableStateOf(DecryptType.Polyalphabetic) }
    var decryptTypeText by remember { mutableStateOf("Polialfabetyczne") }
    val showAlertDialog = remember { mutableStateOf(false) }
    val decipheredText by remember { mutableStateOf(viewModel.decryptedText) }

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
        SelectDecryptionType(
            modifier,
            showAlertDialog
        ) { selectedDecryptedType, selectedDecryptedText ->
            decryptType = selectedDecryptedType
            decryptTypeText = selectedDecryptedText
        }
    }

    LazyColumn(
        modifier
            .fillMaxSize()
            .padding(all = 8.dp)
    ) {
        item { DecipherTopBar(modifier) { onNavigateBack() } }

        item {
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
                    Text(text = decryptTypeText)
                    Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            }
        }

        item {
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

            if (decryptType != DecryptType.Transposition && decryptType != DecryptType.CheckCertificate) {
                TextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = {
                        if(decryptType == DecryptType.CheckSignature) {
                            Text("Wprowadź zaszyfrowany podpis")
                        } else {
                            Text(stringResource(R.string.type_key))
                        }
                    },
                    isError = keyText.isEmpty()
                )
            }

            if (decryptType == DecryptType.CheckSignature) {
                TextField(
                    value = publicKeyText,
                    onValueChange = { publicKeyText = it },
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = {
                        Text(stringResource(R.string.type_public_key))
                    },
                    isError = publicKeyText.isEmpty()
                )
            }
        }

        item {Row(
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (decryptType != DecryptType.Polyalphabetic && decryptType != DecryptType.Transposition  && decryptType != DecryptType.CheckCertificate) {
                Button(
                    onClick = {
                        filePicker.launch(arrayOf("*/*"))
                    }
                ) {
                    Text(stringResource(R.string.select_file))
                }
            } else {
                Spacer(modifier)
            }

            Button(
                onClick = {
                    if (decryptType != DecryptType.Transposition && decryptType != DecryptType.CheckCertificate) {
                        if (keyText.isNotEmpty()) {
                            viewModel.decryptText(
                                inputText,
                                keyText,
                                publicKeyText,
                                decryptType
                            )
                        }
                    } else if (decryptType == DecryptType.CheckSignature) {
                        if (keyText.isNotEmpty() && publicKeyText.isNotEmpty()) {
                            viewModel.decryptText(
                                inputText,
                                keyText,
                                publicKeyText,
                                decryptType
                            )
                        }
                    } else {
                        viewModel.decryptText(
                            inputText,
                            keyText,
                            publicKeyText,
                            decryptType
                        )
                    }
                },
            ) {
                if (decryptType == DecryptType.CheckCertificate) {
                    Text(stringResource(R.string.check_certificate))
                } else {
                    Text(stringResource(R.string.decrypt))
                }
            }
        } }

        item { Spacer(modifier = modifier.padding(4.dp)) }
        item {
            if (decryptType == DecryptType.CheckCertificate) {
                Text(
                    stringResource(R.string.certificate),
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    stringResource(R.string.decrypted_text),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        item { Text(text = decipheredText.value) }

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

        Text(stringResource(R.string.decrypt), style = MaterialTheme.typography.titleMedium)

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

@Composable
fun SelectDecryptionType(
    modifier: Modifier,
    showAlertDialog: MutableState<Boolean>,
    onDismiss: (DecryptType, String) -> Unit
) {
    val decryptTypes = arrayOf(
        "Polialfabetyczne",
        "Przestawieniowe",
        "AES/CBC",
        "DES/CBC",
        "DES/OFB",
        "AES/CFB",
        "RSA",
        "Sprawdź certyfikat",
        "Sprawdź podpis cyfrowy"
    )
    AlertDialog(onDismissRequest = { showAlertDialog.value = false },
        title = { Text(stringResource(R.string.select_encryption)) },
        text = {
            Column(modifier.fillMaxWidth()) {
                decryptTypes.forEach { decryptType ->
                    Text(
                        text = decryptType,
                        modifier = modifier
                            .fillMaxWidth()
                            .clickable {
                                when (decryptType) {
                                    decryptTypes[0] -> {
                                        onDismiss(DecryptType.Polyalphabetic, decryptType)
                                    }

                                    decryptTypes[1] -> {
                                        onDismiss(DecryptType.Transposition, decryptType)
                                    }

                                    decryptTypes[2] -> {
                                        onDismiss(DecryptType.AES, decryptType)
                                    }

                                    decryptTypes[3] -> {
                                        onDismiss(DecryptType.DES, decryptType)
                                    }

                                    decryptTypes[4] -> {
                                        onDismiss(DecryptType.OFB, decryptType)
                                    }

                                    decryptTypes[5] -> {
                                        onDismiss(DecryptType.CFB, decryptType)
                                    }

                                    decryptTypes[6] -> {
                                        onDismiss(DecryptType.RSA, decryptType)
                                    }

                                    decryptTypes[7] -> {
                                        onDismiss(DecryptType.CheckCertificate, decryptType)
                                    }

                                    decryptTypes[8] -> {
                                        onDismiss(DecryptType.CheckSignature, decryptType)
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
