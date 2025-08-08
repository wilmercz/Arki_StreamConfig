package com.wilsoft.arki_streamconfig.models

data class Invitado(
    val nombre: String,
    val rol: String,
    val tema: String = ""
)