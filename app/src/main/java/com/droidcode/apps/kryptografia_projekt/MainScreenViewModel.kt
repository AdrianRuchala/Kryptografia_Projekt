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
        //szyfr ścieżkowy, odczytywany spiralowo
        var uppercaseText = textToEncrypt.uppercase()
        uppercaseText = uppercaseText.replace(" ", "")
        val textLength = uppercaseText.length
        val key = 3
        val encryptedText = StringBuilder()

        val rail =
            Array(key) { StringBuilder() } //stworzenie listy wierszy, tak aby każdy string był StringBuilderem()

        var row = 0
        var direction = 1 //ustawienie kierunku na jeden aby szyfr się przemieszczał w górę lub w dół

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
}
