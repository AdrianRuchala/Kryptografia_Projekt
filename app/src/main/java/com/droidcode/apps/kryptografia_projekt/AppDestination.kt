package com.droidcode.apps.kryptografia_projekt

interface AppDestination {
    val route: String
}

object Menu: AppDestination {
    override val route = "menu"
}

object Encrypt: AppDestination {
    override val route = "encrypt"
}

object Decipher: AppDestination {
    override val route = "decipher"
}
