package com.example.proyecto_final_dwa

data class Mesa(
    val id: String = "",
    val numero: Int = 0,
    val capacidad: Int = 0,
    val estado: String = "libre" // "libre" | "ocupada" | "reservada"
)