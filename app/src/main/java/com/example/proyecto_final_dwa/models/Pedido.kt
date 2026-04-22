package com.example.proyecto_final_dwa

data class ItemPedido(
    val productoId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 0
) {
    fun subtotal() = precio * cantidad
}

data class Pedido(
    val id: String = "",
    val empleadoId: String = "",
    val empleadoNombre: String = "",
    val tipo: String = "mesa",
    val mesaId: String = "",
    val mesaNumero: Int = 0,
    val clienteNombre: String = "",
    val estado: String = "pendiente",
    val items: List<ItemPedido> = emptyList(),
    val total: Double = 0.0,
    val fecha: com.google.firebase.Timestamp? = null
)