package com.droidcode.apps.kryptografia_projekt

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow
import kotlin.math.sqrt

class DecryptViewModel : ViewModel() {

    val decryptedText = mutableStateOf("")

    fun decryptText(
        textToDecrypt: String,
        key: String,
        publicKey: String,
        decryptType: DecryptType
    ) {
        when (decryptType) {
            DecryptType.Polyalphabetic -> {
                decipherPolyalphabetic(textToDecrypt, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.Transposition -> {
                decipherTransposition(textToDecrypt) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.AES -> {
                decipherAES(textToDecrypt, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.DES -> {
                decipherDES(textToDecrypt, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.OFB -> {
                decipherOFB(textToDecrypt, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.CFB -> {
                decipherCFB(textToDecrypt, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.DiffieHellman -> {
                decipherDiffieHellman(
                    textToDecrypt.toLong(),
                    key.toLong(),
                    publicKey.toLong()
                ) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.RSA -> {
                decipherRSA(textToDecrypt, key) { newText ->
                    decryptedText.value = newText
                }
            }

            DecryptType.CheckCertificate -> {
                viewModelScope.launch {
                    checkCertificate(textToDecrypt) { newText ->
                        decryptedText.value = newText
                    }
                }
            }

            DecryptType.CheckSignature -> {
                checkSignature(textToDecrypt.toByteArray(), key, publicKey) { newText ->
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

    private fun decipherRSA(encryptedText: String, key: String, onSuccess: (String) -> Unit) {
        try {
            val keyBytes = Base64.getDecoder().decode(key)
            val keyFactory = KeyFactory.getInstance("RSA") // Tworzenie instancji algorytmu RSA
            val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(keyBytes)) as RSAPrivateKey

            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding") // Ustawienia zgodne z funkcją szyfrującą
            cipher.init(Cipher.DECRYPT_MODE, privateKey) // Inicjalizacja w trybie rozszyfrowania

            val encryptedData = Base64.getDecoder().decode(encryptedText)
            val maxBlockSize = privateKey.modulus.bitLength() / 8
            val decryptedData = mutableListOf<Byte>()

            var offset = 0
            while (offset < encryptedData.size) {
                val chunkSize = minOf(maxBlockSize, encryptedData.size - offset) // Rozmiar bloku
                val chunk = encryptedData.copyOfRange(offset, offset + chunkSize)
                val decryptedChunk = cipher.doFinal(chunk) // Rozszyfrowanie bloku
                decryptedData.addAll(decryptedChunk.toList())
                offset += chunkSize
            }

            val decryptedText = String(decryptedData.toByteArray(), Charsets.UTF_8) // Konwersja na tekst
            onSuccess(decryptedText)
        } catch (e: Exception) {
            onSuccess("Błąd rozszyfrowania")
        }
    }

    private suspend fun checkCertificate(urlString: String, onSuccess: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient() //stowrzenie instacji klienta przy użyciu biblioteki OkHttp
                val request = Request.Builder().url(urlString).build() //żadanie Http z przekazywanym adresem URL

                try {
                    val response = client.newCall(request).execute() //synchroniczne żadanie Http przy użyciu OkHttpClient

                    val certificates = response.handshake?.peerCertificates ?: emptyList()
                    //Pobiera certyfikaty SSL z HTTP

                    for (certificate in certificates) { //iterujemy liste certyfikatów
                        val cert = certificate as X509Certificate //rzutujemy obiekt certyfikatu na X509Certificate, który umożliwia odczytanie szczegółowych danych
//                        onSuccess(
//                            "Issuer: ${cert.issuerDN} \n" +
//                                    "Subject: ${cert.subjectDN} \n" +
//                                    "Valid From: ${cert.notBefore} \n" +
//                                    "Valid To: ${cert.notAfter} \n"
//                        )
                        onSuccess("$cert")
                    }
                } catch (e: Exception) {
                    onSuccess("Błąd analizy certyfikatu SSL")
                }
            } catch (e: Exception) {
                onSuccess("Niepoprawny adres URL. Przykładowy adres URL: https://example.com")
            }
        }
    }

    private fun checkSignature(data: ByteArray, signatureString: String, publicKeyString: String, onSuccess: (String) -> Unit) {
        try {
            val keyBytes = Base64.getDecoder().decode(publicKeyString) //dekodujemy ciąg znaków na tablicę bajtów
            val keyFactory = KeyFactory.getInstance("RSA") //tworzymy instację dla RSA
            val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(keyBytes)) as RSAPublicKey
            //tworzymy specyfikację klucza publicznego w formacie X509EncodedKeySpec i generujemy klucz publiczny

            val signature = Signature.getInstance("SHA256withRSA") //tworzymy instację obiektu z algorytmem SHA256withRSA
            signature.initVerify(publicKey) //inicjalizujemy obiekt, weryfikujemy klucz publiczny
            signature.update(data) //przekazujemy dane wejściowe data do obiektu signature

            val decodedSignature = Base64.getDecoder().decode(signatureString) //dekodujemy ciąg znaków na tablicę bajtów
            signature.verify(decodedSignature)  //porównujemy podpis z obliczonym podpisem dla danych data
            onSuccess("Podpis cyfrowy jest prawidłowy")
        } catch (e: Exception) {
            onSuccess("Weryfikacja nie powiodła się")
        }
    }
}
