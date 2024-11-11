package com.droidcode.apps.kryptografia_projekt

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DecipherViewModel : ViewModel() {

    val decipheredText = mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.O)
    fun decipherText(textToDecipher: String, key: String, encryptType: EncryptType) {
        when (encryptType) {
            EncryptType.Polyalphabetic -> {
                decipherPolyalphabetic(textToDecipher, key) { newText ->
                    decipheredText.value = newText
                }
            }

            EncryptType.Transposition -> {
                decipherTransposition(textToDecipher) { newText ->
                    decipheredText.value = newText
                }
            }

            EncryptType.AES -> {
                decipherAES(textToDecipher, key) { newText ->
                    decipheredText.value = newText
                }
            }

            EncryptType.DES -> {
                decipherDES(textToDecipher, key) { newText ->
                    decipheredText.value = newText
                }
            }

            EncryptType.OFB -> {
                decipherOFB(textToDecipher, key) { newText ->
                    decipheredText.value = newText
                }
            }

            EncryptType.CFB -> {
                decipherCFB(textToDecipher, key) { newText ->
                    decipheredText.value = newText
                }
            }

            EncryptType.DiffieHellman -> {

            }

            EncryptType.RSA -> {

            }
        }
    }

    private fun decipherPolyalphabetic(
        textToDecipher: String,
        key: String,
        onSuccess: (String) -> Unit
    ) {
        val uppercaseText = textToDecipher.uppercase()
        val uppercaseKey = key.uppercase()
        val textIndices = uppercaseText.indices
        val repeatedKey = uppercaseKey.repeat((uppercaseText.length / uppercaseKey.length) + 1)
        val decryptedText = StringBuilder()

        for (i in textIndices) {
            val char = uppercaseText[i]
            if (char.isLetter()) {
                val offset = 'A'
                val keyOffset = repeatedKey[i] - 'A'
                decryptedText.append(((char - offset - keyOffset + 26) % 26 + offset.code).toChar())
            } else {
                decryptedText.append(char)
            }
        }
        onSuccess(decryptedText.toString())
    }

    private fun decipherTransposition(textToDecipher: String, onSuccess: (String) -> Unit) {
        var uppercaseText = textToDecipher.uppercase()
        uppercaseText = uppercaseText.replace(" ", "")
        val textLength = uppercaseText.length
        val key = 3

        val rail = Array(key) { CharArray(textLength) { '\n' } }

        var row = 0
        var direction = 1

        for (i in 0 until textLength) {
            rail[row][i] = '*'  // zaznaczamy pozycje na płotku
            if (row == 0) {
                direction = 1
            } else if (row == key - 1) {
                direction = -1
            }
            row += direction
        }

        var index = 0
        for (r in 0 until key) {
            for (c in 0 until textLength) {
                if (rail[r][c] == '*' && index < textLength) {
                    rail[r][c] =
                        uppercaseText[index++] // wypełniamy znakami na zaszyfrowanych pozycjach
                }
            }
        }

        val decryptedText = StringBuilder()
        row = 0
        direction = 1
        for (i in 0 until textLength) {
            decryptedText.append(rail[row][i])  //odczytujemy tekst
            if (row == 0) {
                direction = 1
            } else if (row == key - 1) {
                direction = -1
            }
            row += direction
        }
        onSuccess(decryptedText.toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decipherAES(textToDecipher: String, key: String, onSuccess: (String) -> Unit) {
        try {
            val md = MessageDigest.getInstance("MD5")
            val keyBytes = md.digest(key.toByteArray(Charsets.UTF_8))
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

            val ivParameterSpec = IvParameterSpec(ByteArray(16))

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

            val decodedBytes = Base64.getDecoder().decode(textToDecipher)
            val decipheredBytes = cipher.doFinal(decodedBytes)
            val decipheredText = String(decipheredBytes, Charsets.UTF_8)

            onSuccess(decipheredText)

        } catch (e: Exception) {
            onSuccess("Błąd rozszyfrowania")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decipherDES(textToDecipher: String, key: String, onSuccess: (String) -> Unit) {
        try {
            val md = MessageDigest.getInstance("MD5")
            val keyBytes = md.digest(key.toByteArray(Charsets.UTF_8))
            val cuttedKeyBytes = keyBytes.copyOf(8)
            val secretKey = SecretKeySpec(cuttedKeyBytes, "DES")

            val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")

            val ivParameterSpec = IvParameterSpec(ByteArray(8))

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

            val decodedBytes = Base64.getDecoder().decode(textToDecipher)
            val decipheredBytes = cipher.doFinal(decodedBytes)
            val decipheredText = String(decipheredBytes, Charsets.UTF_8)

            onSuccess(decipheredText)

        } catch (e: Exception) {
            onSuccess("Błąd rozszyfrowania")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decipherOFB(textToDecipher: String, key: String, onSuccess: (String) -> Unit) {
        try {
            val md = MessageDigest.getInstance("MD5")
            val keyBytes = md.digest(key.toByteArray(Charsets.UTF_8))
            val cuttedKeyBytes = keyBytes.copyOf(8)
            val secretKey = SecretKeySpec(cuttedKeyBytes, "DES")

            val cipher = Cipher.getInstance("DES/OFB/PKCS5Padding")

            val ivParameterSpec = IvParameterSpec(ByteArray(8))

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

            val decodedBytes = Base64.getDecoder().decode(textToDecipher)
            val decipheredBytes = cipher.doFinal(decodedBytes)
            val decipheredText = String(decipheredBytes, Charsets.UTF_8)

            onSuccess(decipheredText)

        } catch (e: Exception) {
            onSuccess("Błąd rozszyfrowania")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decipherCFB(textToDecipher: String, key: String, onSuccess: (String) -> Unit) {
        try {
            val md = MessageDigest.getInstance("MD5")
            val keyBytes = md.digest(key.toByteArray(Charsets.UTF_8))
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/CFB/PKCS5Padding")

            val ivParameterSpec = IvParameterSpec(ByteArray(16))

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

            val decodedBytes = Base64.getDecoder().decode(textToDecipher)
            val decipheredBytes = cipher.doFinal(decodedBytes)
            val decipheredText = String(decipheredBytes, Charsets.UTF_8)

            onSuccess(decipheredText)

        } catch (e: Exception) {
            onSuccess("Błąd rozszyfrowania")
        }
    }
}
