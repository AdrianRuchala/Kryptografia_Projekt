package com.droidcode.apps.kryptografia_projekt

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainScreenViewModel : ViewModel() {

    val encryptedText = mutableStateOf("")

    fun encryptText(textToEncrypt: String, key: String, encryptType: EncryptType) {
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
        //szyfr macierzowy
        var uppercaseText = textToEncrypt.uppercase()
        uppercaseText = uppercaseText.replace(" ", "")
        val textLength = uppercaseText.length
        val rows = 3
        var columns = textLength / rows
        var textIndex = 0
        val encryptedText = StringBuilder()

        if (textLength % rows != 0) {
            columns += 1
        }

        val array =
            Array(rows) { CharArray(columns) } //charArray - tablica dla znaków, stworzenie macierzy

        for (i in 0 until rows) {
            for (j in 0 until columns) {
                if (textIndex < textLength) {
                    array[i][j] = uppercaseText[textIndex]
                    textIndex++
                } else {
                    array[i][j] = 'X'    //jeżeli jest puste miejsce to na jego miejscu jest X
                }
            }
        }

        for (j in 0 until columns) {   // odczytujemy kolumnowo
            for (i in 0 until rows) {
                encryptedText.append(array[i][j])
            }
        }

        onSuccess(encryptedText.toString())
    }
}
