package com.droidcode.apps.kryptografia_projekt

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import android.Manifest
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.net.toUri
import java.io.File
import java.security.KeyPairGenerator
import java.util.Base64

private var mediaRecorder: MediaRecorder? = null
private var audioFile: File? = null

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EncryptScreen(modifier: Modifier, viewModel: EncryptViewModel, onNavigateBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var keyText by remember { mutableStateOf("") }
    var secretKeyText1 by remember { mutableStateOf("") }
    var secretKeyText2 by remember { mutableStateOf("") }
    var encryptionType by remember { mutableStateOf(EncryptType.Polyalphabetic) }
    var encryptionTypeText by remember { mutableStateOf("Polialfabetyczne") }
    val showAlertDialog = remember { mutableStateOf(false) }
    val encryptedText by remember { mutableStateOf(viewModel.encryptedText) }
    var isRecording by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            if (keyText.isNotEmpty()) {
                viewModel.encryptFile(uri, keyText, encryptionType, context)
            } else {
                Toast.makeText(
                    context,
                    getString(context, R.string.empty_key_error),
                    Toast.LENGTH_LONG
                ).show()
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

    LazyColumn(
        modifier
            .fillMaxSize()
            .padding(all = 8.dp)
    ) {
        item { TopBar(modifier) { onNavigateBack() } }

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
                    Text(text = encryptionTypeText)
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
                    if (encryptionType == EncryptType.DiffieHellman) {
                        Text(stringResource(R.string.prime_number))
                    } else {
                        Text(stringResource(R.string.type_text))
                    }
                }
            )

            if (encryptionType != EncryptType.Transposition && encryptionType != EncryptType.CheckCertificate) {
                TextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = {
                        if (encryptionType == EncryptType.DiffieHellman) {
                            Text(stringResource(R.string.base_number))
                        } else {
                            Text(stringResource(R.string.type_key))
                        }
                    },
                    isError = keyText.isEmpty()
                )
            }

            if (encryptionType == EncryptType.DiffieHellman) {
                TextField(
                    value = secretKeyText1,
                    onValueChange = { secretKeyText1 = it },
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = {
                        Text(stringResource(R.string.secret_key1))
                    },
                    isError = secretKeyText1.isEmpty()
                )

                TextField(
                    value = secretKeyText2,
                    onValueChange = { secretKeyText2 = it },
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = {
                        Text(stringResource(R.string.secret_key2))
                    },
                    isError = secretKeyText2.isEmpty()
                )
            }
        }

        item {
            Row(
                modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (encryptionType != EncryptType.Polyalphabetic && encryptionType != EncryptType.Transposition && encryptionType != EncryptType.DiffieHellman && encryptionType != EncryptType.CheckCertificate) {
                    Column {
                        Button(
                            onClick = {
                                filePicker.launch(arrayOf("*/*"))
                            }
                        ) {
                            Text(stringResource(R.string.select_file))
                        }

                        Button(
                            onClick = {
                                if(isAudioPermissionGranted(context) && keyText.isNotEmpty()){
                                    if (isRecording) {
                                        stopRecording(viewModel, keyText, encryptionType, context)
                                        isRecording = !isRecording
                                    } else {
                                        startRecording(context)
                                        isRecording = !isRecording
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.empty_key_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                        ) {
                            Text(
                                if (isRecording) stringResource(R.string.end_recording) else stringResource(
                                    R.string.start_recording
                                )
                            )
                        }
                    }
                } else {
                    Spacer(modifier)
                }

                Button(
                    onClick = {
                        if (encryptionType != EncryptType.Transposition && encryptionType != EncryptType.DiffieHellman && encryptionType != EncryptType.CheckCertificate) {
                            if (keyText.isNotEmpty()) {
                                viewModel.encryptText(
                                    inputText,
                                    keyText,
                                    secretKeyText1,
                                    secretKeyText2,
                                    encryptionType
                                )
                            }
                        } else if (encryptionType == EncryptType.DiffieHellman) {
                            if (keyText.isNotEmpty() && secretKeyText1.isNotEmpty() && secretKeyText2.isNotEmpty()) {
                                viewModel.encryptText(
                                    inputText,
                                    keyText,
                                    secretKeyText1,
                                    secretKeyText2,
                                    encryptionType
                                )
                            }
                        } else {
                            viewModel.encryptText(
                                inputText,
                                keyText,
                                secretKeyText1,
                                secretKeyText2,
                                encryptionType
                            )
                        }
                    },
                ) {
                    if (encryptionType == EncryptType.CheckCertificate) {
                        Text(stringResource(R.string.check_certificate))
                    } else {
                        Text(stringResource(R.string.encrypt))
                    }
                }
            }
        }


        if (encryptionType == EncryptType.RSA) {
            item {
                Button(onClick = {
                    generateKey { generatedKey ->
                        keyText = generatedKey
                    }
                }) {
                    Text(stringResource(R.string.generate_key))
                }
            }

        }

        item { Spacer(modifier = modifier.padding(4.dp)) }
        item {
            if (encryptionType == EncryptType.CheckCertificate) {
                Text(
                    stringResource(R.string.certificate),
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    stringResource(R.string.encrypted_text),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        item { Text(text = encryptedText.value) }

    }
}

@Composable
fun TopBar(modifier: Modifier, onNavigateBack: () -> Unit) {
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
        "Przestawieniowe",
        "AES/CBC",
        "DES/CBC",
        "DES/OFB",
        "AES/CFB",
        "Diffie-Hellman",
        "RSA",
        "Sprawdź certyfikat"
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

                                    encryptTypes[2] -> {
                                        onDismiss(EncryptType.AES, encryptType)
                                    }

                                    encryptTypes[3] -> {
                                        onDismiss(EncryptType.DES, encryptType)
                                    }

                                    encryptTypes[4] -> {
                                        onDismiss(EncryptType.OFB, encryptType)
                                    }

                                    encryptTypes[5] -> {
                                        onDismiss(EncryptType.CFB, encryptType)
                                    }

                                    encryptTypes[6] -> {
                                        onDismiss(EncryptType.DiffieHellman, encryptType)
                                    }

                                    encryptTypes[7] -> {
                                        onDismiss(EncryptType.RSA, encryptType)
                                    }

                                    encryptTypes[8] -> {
                                        onDismiss(EncryptType.CheckCertificate, encryptType)
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

fun generateKey(onSuccess: (String) -> Unit) {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(2048)
    val publicKey = keyPairGenerator.generateKeyPair().public
    val publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.encoded)
    onSuccess(publicKeyBase64)
}

private fun startRecording(context: Context) {
    try {
        // Tworzenie pliku do zapisania nagrania
        val outputDir = context.cacheDir
        audioFile = File.createTempFile("voice_message", ".m4a", outputDir)

        // Przygotowanie MediaRecorder
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        mediaRecorder?.release()
        mediaRecorder = null
        Toast.makeText(context, "Błąd przy rozpoczęciu nagrywania", Toast.LENGTH_SHORT).show()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun stopRecording(
    viewModel: EncryptViewModel,
    key: String,
    encryptType: EncryptType,
    context: Context
) {
    try {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        audioFile?.let {
            viewModel.encryptFile(it.toUri(), key, encryptType, context)
        }
    } catch (e: IllegalStateException) {
        e.printStackTrace()
        Toast.makeText(context, "Błąd przy zatrzymywaniu nagrywania", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Wystąpił błąd. Spróbuj ponownie", Toast.LENGTH_SHORT).show()
    } finally {
        mediaRecorder = null
        audioFile = null
    }
}

fun isAudioPermissionGranted(context: Context): Boolean {
    val permission = Manifest.permission.RECORD_AUDIO
    if (ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        return true
    } else {
        Toast.makeText(
            context,
            context.getString(R.string.permission_not_granted_error),
            Toast.LENGTH_SHORT
        ).show()
        return false
    }
}

