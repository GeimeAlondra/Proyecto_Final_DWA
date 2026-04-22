package com.example.proyecto_final_dwa

data class Empleado(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val puesto: String = "",
    val activo: Boolean = true
)