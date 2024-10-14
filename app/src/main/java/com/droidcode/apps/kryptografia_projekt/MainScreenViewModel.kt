package com.droidcode.apps.kryptografia_projekt

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainScreenViewModel : ViewModel() {

    val encryptedText = mutableStateOf("")

    fun encryptText(textToEncrypt: String, encryptType: EncryptType) {
        when (encryptType) {
            EncryptType.Monoalphabetic -> {
                encryptMonoalphabetic(textToEncrypt) { newText ->
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

    private fun encryptMonoalphabetic(textToEncrypt: String, onSuccess: (String) -> Unit) {
        var uppercaseText = textToEncrypt.uppercase()
        uppercaseText = uppercaseText.replace(" ", "")
        val encryptedText = StringBuilder() //StringBuilder() służy do tworzenia i manipulowania
        // łańcuchem znaków, zwykłe stringi są niemutowalne, dlatego jest użyta klasa StringBuilder
        val key = mapOf(
            'A' to 'C',
            'B' to 'D',
            'C' to 'E',
            'D' to 'F',
            'E' to 'G',
            'F' to 'H',
            'G' to 'I',
            'H' to 'J',
            'I' to 'K',
            'J' to 'L',
            'K' to 'M',
            'L' to 'N',
            'M' to 'O',
            'N' to 'P',
            'O' to 'Q',
            'P' to 'R',
            'Q' to 'S',
            'R' to 'T',
            'S' to 'U',
            'T' to 'V',
            'U' to 'W',
            'V' to 'X',
            'W' to 'Y',
            'X' to 'Z',
            'Y' to 'A',
            'Z' to 'B'
        )

        for (char in uppercaseText) {
            encryptedText.append(key.getOrDefault(char, char))
            //append pozwala dodawać znaki lub ciąg znaków do końca StringBuildera, czyli encryptedText
            //getOrDefault pobiera wartość dla danej litery z mapy key, jeżeli istnieje
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

        val array = Array(rows) { CharArray(columns) } //charArray - tablica dla znaków, stworzenie macierzy

        for (i in 0 until rows) {
            for (j in 0 until columns) {
                if (textIndex < textLength) {
                    array[i][j] = uppercaseText[textIndex]
                    textIndex ++
                } else {
                    array[i][j] = 'X'    //jeżeli jest puste miejsce to na jego miejscu jest X
                }
            }
        }

        for (j in 0 until columns){   // odczytujemy kolumnowo
            for (i in 0 until rows) {
                encryptedText.append(array[i][j])
            }
        }

        onSuccess(encryptedText.toString())
    }
}
