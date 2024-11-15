package com.droidcode.apps.kryptografia_projekt

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow
import kotlin.math.sqrt


class EncryptViewModel : ViewModel() {

    val encryptedText = mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptText(
        textToEncrypt: String,
        key: String,
        secretKey1: String,
        secretKey2: String,
        encryptType: EncryptType
    ) {
        when (encryptType) {
            EncryptType.Polyalphabetic -> {
                encryptPolyalphabetic(textToEncrypt, key) { newText ->
                    encryptedText.value = newText
                }
            }

            EncryptType.Transposition -> {
                encryptTransposition(textToEncrypt) { newText ->
                    encryptedText.value = newText
                }
            }

            EncryptType.AES -> {
                encryptAES(textToEncrypt.toByteArray(), key) { newText ->
                    encryptedText.value = newText
                }
            }

            EncryptType.DES -> {
                encryptDES(textToEncrypt.toByteArray(), key) { newText ->
                    encryptedText.value = newText
                }
            }

            EncryptType.OFB -> {
                encryptOFB(textToEncrypt.toByteArray(), key) { newText ->
                    encryptedText.value = newText
                }
            }

            EncryptType.CFB -> {
                encryptCFB(textToEncrypt.toByteArray(), key) { newText ->
                    encryptedText.value = newText
                }
            }

            EncryptType.DiffieHellman -> {
                diffieHellmanKeyExchange(
                    textToEncrypt.toLong(),
                    key.toLong(),
                    secretKey1.toLong(),
                    secretKey2.toLong()
                ) { newText ->
                    encryptedText.value = newText
                }
            }

            EncryptType.RSA -> {
                encryptRSA(textToEncrypt.toByteArray(), key) { newText ->
                    encryptedText.value = newText
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptFile(uri: Uri, key: String, encryptType: EncryptType, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val fileBytes = inputStream?.readBytes() ?: return@launch

            when (encryptType) {
                EncryptType.Polyalphabetic -> {}

                EncryptType.Transposition -> {}

                EncryptType.AES -> {
                    encryptAES(fileBytes, key) { newText ->
                        saveEncryptedFile(newText.toByteArray(), context)
                        encryptedText.value = getString(context, R.string.file_saved)
                    }
                }

                EncryptType.DES -> {
                    encryptDES(fileBytes, key) { newText ->
                        saveEncryptedFile(newText.toByteArray(), context)
                        encryptedText.value = getString(context, R.string.file_saved)
                    }
                }

                EncryptType.OFB -> {
                    encryptOFB(fileBytes, key) { newText ->
                        saveEncryptedFile(newText.toByteArray(), context)
                        encryptedText.value = getString(context, R.string.file_saved)
                    }
                }

                EncryptType.CFB -> {
                    encryptCFB(fileBytes, key) { newText ->
                        saveEncryptedFile(newText.toByteArray(), context)
                        encryptedText.value = getString(context, R.string.file_saved)
                    }
                }

                EncryptType.DiffieHellman -> {}

                EncryptType.RSA -> {
                    encryptRSA(fileBytes, key) { newText ->
                        saveEncryptedFile(newText.toByteArray(), context)
                        encryptedText.value = getString(context, R.string.file_saved)
                    }
                }
            }
        }
    }

    private fun encryptPolyalphabetic(
        textToEncrypt: String,
        key: String,
        onSuccess: (String) -> Unit
    ) {
        val uppercaseText = textToEncrypt.uppercase()
        val uppercaseKey = key.uppercase()
        val textIndices = uppercaseText.indices  //liczba indeksów w tekście do zaszyfrowania
        val repeatedKey =
            uppercaseKey.repeat((uppercaseText.length / uppercaseKey.length) + 1)
        // powtarzanie klucza aby jego długość była taka sama jak długość tekstu do zaszyfrowania
        val encryptedText = StringBuilder()

        for (i in textIndices) {
            val char = uppercaseText[i]
            if (char.isLetter()) {
                val offset = 'A'
                val keyOffset = repeatedKey[i] - 'A'
                encryptedText.append(((char - offset + keyOffset) % 26 + offset.code).toChar())
                // char - offset: przesuwamy znak na numer od 0 do 25, A=0, Z=25
                // + keyOffset: dodajemy przesunięcie wynikające z odpowiedniej litery klucza,
                // % 26: wynik mieści się z zakresie,
                // offset.code: przekształca wynik na kod ASCII, dzięki czemu uzyskujemy odpowiednią literę.
                // toChar(): konwertuje kod ASCII na znak
            } else {
                encryptedText.append(char) // jeśli znak nie jest literą to zostaje bez zmian
            }
        }
        onSuccess(encryptedText.toString())
    }

    private fun encryptTransposition(textToEncrypt: String, onSuccess: (String) -> Unit) {
        //szyfr płotkowy
        var uppercaseText = textToEncrypt.uppercase()
        uppercaseText = uppercaseText.replace(" ", "")
        val textLength = uppercaseText.length
        val key = 3
        val encryptedText = StringBuilder()

        val rail =
            Array(key) { StringBuilder() } //stworzenie listy wierszy, tak aby każdy string był StringBuilderem()

        var row = 0
        var direction =
            1 //ustawienie kierunku na jeden aby szyfr się przemieszczał w górę lub w dół

        for (i in 0 until textLength) { //wypełnianie macierzy
            rail[row].append(uppercaseText[i]) //dodanie znaku do wiersza
            if (row == 0) {
                direction = 1
            } else if (row == key - 1) {
                direction = -1  //zmiana kierunku gdy jesteśmy na dole lub na górze
            }
            row += direction    //zmiana wiersza w zależności od kierunku
        }

        for (i in rail) {
            encryptedText.append(i) //dodanie znaku to końcowego tekstu
        }

        onSuccess(encryptedText.toString())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptAES(textToEncrypt: ByteArray, key: String, onSuccess: (String) -> Unit) {
        //AES blokowy
        val md = MessageDigest.getInstance("MD5") //stworzenie obiektu do generowania hashy
        // w MD5 - 128 bitów

        val keyBytes =
            md.digest(key.toByteArray(Charsets.UTF_8)) //konwersja tekstu na tablice bajtów
        val secretKey = SecretKeySpec(keyBytes, "AES") //stworzenie obiektu klucza dla algorytmu AES

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") // stworzenie obiektu
        //szyfrującego algorytmem AES w trybie CBC (blokowym) oraz wypełnienie PKCS5

        val ivParameterSpec = IvParameterSpec(ByteArray(16)) // ustawienie wektora
        // inicjalizującago na tablicę zer o długości 16 bajtów

        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKey,
            ivParameterSpec
        ) // inicjalizacja obiektu w trybie szyfrowania, przy użyciu klucza oraz IV.

        val encryptResult = cipher.doFinal(textToEncrypt) //szyfrowanie tekstu
        val encryptedText =
            Base64.getEncoder().encodeToString(encryptResult) //konwersja tekstu na string

        onSuccess(encryptedText.toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptDES(textToEncrypt: ByteArray, key: String, onSuccess: (String) -> Unit) {
        //DES blokowy
        val md = MessageDigest.getInstance("MD5")
        val keyBytes = md.digest(key.toByteArray(Charsets.UTF_8))
        val cuttedKeyBytes = keyBytes.copyOf(8)
        val secretKey = SecretKeySpec(cuttedKeyBytes, "DES")

        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding") // stworzenie obiektu
        //szyfrującego algorytmem DES w trybie CBC (blokowym) oraz wypełnienie PKCS5

        val ivParameterSpec = IvParameterSpec(ByteArray(8)) // ustawienie wektora
        // inicjalizującago na tablicę zer o długości 8 bajtów

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encryptResult = cipher.doFinal(textToEncrypt)
        val encryptedText = Base64.getEncoder().encodeToString(encryptResult)

        onSuccess(encryptedText.toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptOFB(textToEncrypt: ByteArray, key: String, onSuccess: (String) -> Unit) {
        //DES strumieniowy
        val md = MessageDigest.getInstance("MD5")
        val keyBytes = md.digest(key.toByteArray(Charsets.UTF_8))
        val cuttedKeyBytes = keyBytes.copyOf(8)
        val secretKey = SecretKeySpec(cuttedKeyBytes, "DES")

        val cipher = Cipher.getInstance("DES/OFB/PKCS5Padding") // stworzenie obiektu
        //szyfrującego algorytmem DES w trybie OFB (strumieniowym) oraz wypełnienie PKCS5

        val ivParameterSpec = IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encryptResult = cipher.doFinal(textToEncrypt)
        val encryptedText = Base64.getEncoder().encodeToString(encryptResult)

        onSuccess(encryptedText.toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptCFB(textToEncrypt: ByteArray, key: String, onSuccess: (String) -> Unit) {
        //AES strumieniowy
        val md = MessageDigest.getInstance("MD5")
        val keyBytes = md.digest(key.toByteArray(Charsets.UTF_8))
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/CFB/PKCS5Padding") // stworzenie obiektu
        //szyfrującego algorytmem AES w trybie CFB (strumieniowym) oraz wypełnienie PKCS5

        val ivParameterSpec = IvParameterSpec(ByteArray(16))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encryptResult = cipher.doFinal(textToEncrypt)
        val encryptedText = Base64.getEncoder().encodeToString(encryptResult)

        onSuccess(encryptedText.toString())
    }

    private fun diffieHellmanKeyExchange(
        primeNumber: Long,
        baseNumber: Long,
        privateKey1: Long,
        privateKey2: Long,
        onSuccess: (String) -> Unit
    ) {
        try {
            val typedNumber = primeNumber.toInt()
            if (typedNumber < 2) {  //sprawdzamy czy podana liczba jest liczbą pierwszą
                onSuccess("Wprowadzona liczba nie jest liczbą pierwszą")
            } else {
                for (i in 2..sqrt(typedNumber.toDouble()).toInt()) {
                    if (typedNumber % i == 0) {
                        onSuccess("Wprowadzona liczba nie jest liczbą pierwszą")
                    } else {

                        fun calculatePower(x: Long, y: Long, primeNumber: Long): Long {
                            return if (y == 1L) {
                                x   //jeżeli wykładnik jest równy 1 to zwracamy podstawe
                            } else {
                                (x.toDouble().pow(y.toDouble()).toLong()) % primeNumber
                                //zwracamy wynik potęgowania
                            }
                        }

                        //obliczamy klucze publiczne
                        val publicKey1 = calculatePower(baseNumber, privateKey1, primeNumber)
                        val publicKey2 = calculatePower(baseNumber, privateKey2, primeNumber)

                        //obliczamy klucze sekretne
                        val secretKey1 = calculatePower(publicKey2, privateKey1, primeNumber)
                        val secretKey2 = calculatePower(publicKey1, privateKey2, primeNumber)

                        if (secretKey1 == secretKey2) {
                            onSuccess("Wspólny sekretny klucz: $secretKey1")
                        } else {
                            onSuccess("Sekretny klucz 1= $secretKey1, Sekretny klucz 2 = $secretKey2")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            onSuccess("Wprowadzony tekst nie jest liczbą pierwszą")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptRSA(textToEncrypt: ByteArray, key: String, onSuccess: (String) -> Unit) {
        val keyBytes = Base64.getDecoder().decode(key) //dekodowanie klucza
        val keyFactory = KeyFactory.getInstance("RSA") //stworzenie instacji do generowania klucza dla algorytmu RSA
        val publicKey = keyFactory.generatePublic(java.security.spec.X509EncodedKeySpec(keyBytes)) as RSAPublicKey
        //generowanie klucza publicznego z wcześniej dekodowanego klucza

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding") // stworzenie obiektu
        //szyfrującego algorytmem RSA w trybie ECB oraz wypełnienie PKCS1
        cipher.init(Cipher.ENCRYPT_MODE, publicKey) //inicjalizacja szyfrowania

        val maxBlockSize = publicKey.modulus.bitLength() / 8 - 11 //obliczenie maksymalnego rozmiaru bloku
        val encryptedData = mutableListOf<Byte>() //stworzenie listy przechowywującej zaszyfrowane dane

        var offset = 0
        while (offset < textToEncrypt.size) {
            val chunkSize = minOf(maxBlockSize, textToEncrypt.size - offset) //określenie rozmiaru aktualnego bloku
            val chunk = textToEncrypt.copyOfRange(offset, offset + chunkSize) // kopiowanie danych z danego fragmentu
            val encryptedChunk = cipher.doFinal(chunk) //szyfrowanie danego bloku danych
            encryptedData.addAll(encryptedChunk.toList()) //dodanie bloku do listy
            offset += chunkSize //przesuniecie offsetu do kolejnego bloku danych
        }

        val encryptedText = Base64.getEncoder().encodeToString(encryptedData.toByteArray())
        //złączenie wszysykich zaszyfrowanych danych

        onSuccess(encryptedText)
    }


    private fun saveEncryptedFile(encryptedBytes: ByteArray, context: Context) {
        val contentValues = ContentValues().apply { //stworzenie pliku txt
            put(MediaStore.MediaColumns.DISPLAY_NAME, "encrypted_file.txt") //ustawienie nazwy pliku
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")    //ustawienie typu pliku
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            ) // ustawienie folderu w którym ma być zapisany plik
        }

        val uri: Uri? = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues   //pobieramy URI pliku aby potem zapisać w nim tekst
        )

        uri?.let {
            context.contentResolver.openOutputStream(it).use { outputStream: OutputStream? ->
                outputStream?.write(encryptedBytes) //wypełnienie pliku zaszyfrowanymi bitami
            }
        }
    }
}
