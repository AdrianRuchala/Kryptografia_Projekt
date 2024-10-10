package com.droidcode.apps.kryptografia_projekt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(modifier: Modifier) {
    val viewModel = MainScreenViewModel()

    Scaffold { padding ->
        View(modifier.padding(padding), viewModel)
    }
}

@Composable
fun View(modifier: Modifier, viewModel: MainScreenViewModel) {
    var inputText by remember { mutableStateOf("") }
    var encryptionType by remember { mutableStateOf(EncryptType.Monoalphabetic) }
    var encryptionTypeText by remember { mutableStateOf("Monoalfabetyczne") }
    val showAlertDialog = remember { mutableStateOf(false) }
    val encryptedText by remember { mutableStateOf(viewModel.encryptedText) }

    if (showAlertDialog.value) {
        SelectEncryptionType(
            Modifier,
            showAlertDialog
        ) { selectedEncryptionType, selectedEncryptionText ->
            encryptionType = selectedEncryptionType
            encryptionTypeText = selectedEncryptionText
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(all = 8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
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
            modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.type_text))
            }
        )

        Button(
            onClick = {
                viewModel.encryptText(inputText, encryptionType)
            },
            Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.encrypt))
        }

        Spacer(modifier = modifier.padding(4.dp))
        Text(stringResource(R.string.encrypted_text), style = MaterialTheme.typography.titleMedium)
        Text(text = encryptedText.value)
    }
}

@Composable
fun SelectEncryptionType(
    modifier: Modifier,
    showAlertDialog: MutableState<Boolean>,
    onDismiss: (EncryptType, String) -> Unit
) {
    val encryptTypes = arrayOf(
        "Monoalfabetyczne",
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
                                        onDismiss(EncryptType.Monoalphabetic, encryptType)
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
