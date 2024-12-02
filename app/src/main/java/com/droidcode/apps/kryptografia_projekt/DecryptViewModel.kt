package com.droidcode.apps.kryptografia_projekt

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateKey
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow
import kotlin.math.sqrt

class DecryptViewModel : ViewModel() {

    val decryptedText = mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptText(
        textToDecipher: String,
        key: String,
        publicKey: String,
        decryptType: DecryptType
    ) {
        when (decryptType) {
            DecryptType.Polyalphabetic -> {
                decipherPolyalphabetic(textToDecipher, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.Transposition -> {
                decipherTransposition(textToDecipher) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.AES -> {
                decipherAES(textToDecipher, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.DES -> {
                decipherDES(textToDecipher, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.OFB -> {
                decipherOFB(textToDecipher, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.CFB -> {
                decipherCFB(textToDecipher, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.DiffieHellman -> {
                decipherDiffieHellman(
                    textToDecipher.toLong(),
                    key.toLong(),
                    publicKey.toLong()
                ) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.RSA -> {
                decipherRSA(textToDecipher, key) { newText ->
                    decryptedText.value = newText
                }
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

    private fun decipherDiffieHellman(
        primeNumber: Long,
        baseNumber: Long,
        publicKey: Long,
        onSuccess: (String) -> Unit
    ) {
        try {
            val typedNumber = primeNumber.toInt()
            if (typedNumber < 2) {
                onSuccess("Wprowadzona liczba nie jest liczbą pierwszą")
                return
            }
            for (i in 2..sqrt(typedNumber.toDouble()).toInt()) {
                if (typedNumber % i == 0) {
                    onSuccess("Wprowadzona liczba nie jest liczbą pierwszą")
                    return
                }
            }

            var privateKey: Long? = null
            for (possibleKey in 1 until primeNumber) {
                val calculatedPublicKey =
                    (baseNumber.toDouble().pow(possibleKey.toDouble()).toLong() % primeNumber)
                if (calculatedPublicKey == publicKey) {
                    privateKey = possibleKey
                    break
                }
            }

            if (privateKey != null) {
                onSuccess("Sekretny klucz: $privateKey")
            } else {
                onSuccess("Błąd rozszyfrowania")
            }
        } catch (e: Exception) {
            onSuccess("Błąd rozszyfrowania")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decipherRSA(encryptedText: String, key: String, onSuccess: (String) -> Unit) {
        try {
            val privateKeyBytes = Base64.getDecoder().decode(key)
            val keyFactory = KeyFactory.getInstance("RSA")
            val privateKey = keyFactory.generatePrivate(java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes)) as RSAPrivateKey

            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)

            val encryptedData = Base64.getDecoder().decode(encryptedText)
            val maxBlockSize = privateKey.modulus.bitLength() / 8
            val decryptedData = mutableListOf<Byte>()

            var offset = 0
            while (offset < encryptedData.size) {
                val chunkSize = minOf(maxBlockSize, encryptedData.size - offset)
                val chunk = encryptedData.copyOfRange(offset, offset + chunkSize)
                val decryptedChunk = cipher.doFinal(chunk)
                decryptedData.addAll(decryptedChunk.toList())
                offset += chunkSize
            }

            val decryptedText = String(decryptedData.toByteArray(), Charsets.UTF_8)
            onSuccess(decryptedText)
        } catch (e: Exception) {
            onSuccess("Błąd rozszyfrowania")
        }
    }
}
