package com.wilsoft.arki_streamconfig

// Modelo de datos para Invitado
data class Invitado(
    val id: String = "",
    val nombre: String = "",
    val rol: String = "",
    val tema: String = "",
    val subTema: String = "",
    val fecha: String = "",
    val lugar: String = "Estudio Principal",
    val timestamp: Long = System.currentTimeMillis(),
    val graficoInvitado: Boolean = false
)