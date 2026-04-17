package com.example.proyecto_final_dwa.models

data class Producto (
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val disponible: Boolean = true
)