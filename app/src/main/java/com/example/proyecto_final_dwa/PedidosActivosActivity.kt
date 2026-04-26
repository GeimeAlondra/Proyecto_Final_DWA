package com.example.proyecto_final_dwa

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.proyecto_final_dwa.databinding.ActivityPedidosActivosBinding

class PedidosActivosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPedidosActivosBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PedidoAdapter
    private val listaPedidos = mutableListOf<Pedido>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidosActivosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecycler()
        cargarPedidos()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecycler() {
        adapter = PedidoAdapter(
            listaPedidos,
            onEntregar = { pedido ->
                AlertDialog.Builder(this)
                    .setTitle("Marcar como entregado")
                    .setMessage("¿Confirmar entrega del pedido #${pedido.id.take(6).uppercase()}?")
                    .setPositiveButton("Confirmar") { _, _ -> actualizarEstado(pedido, "entregado") }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },
            onPagar = { pedido ->
                AlertDialog.Builder(this)
                    .setTitle("Registrar pago")
                    .setMessage("¿Confirmar pago de $%.2f?".format(pedido.total))
                    .setPositiveButton("Pagado") { _, _ -> actualizarEstado(pedido, "pagado") }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerPedidos.layoutManager = LinearLayoutManager(this)
        binding.recyclerPedidos.adapter = adapter
    }

    private fun cargarPedidos() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("pedidos")
            .whereEqualTo("empleadoId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PEDIDOS", "Error: ${error.message}")
                    return@addSnapshotListener
                }

                val nuevos = snapshot?.documents
                    ?.filter { doc ->
                        val estado = doc.getString("estado") ?: ""
                        estado == "pendiente" || estado == "entregado"
                    }
                    ?.map { doc ->
                        @Suppress("UNCHECKED_CAST")
                        val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                        val items = itemsRaw.map { map ->
                            ItemPedido(
                                productoId = map["productoId"] as? String ?: "",
                                nombre = map["nombre"] as? String ?: "",
                                precio = (map["precio"] as? Double) ?: 0.0,
                                cantidad = ((map["cantidad"] as? Long) ?: 0).toInt()
                            )
                        }
                        Pedido(
                            id = doc.id,
                            empleadoId = doc.getString("empleadoId") ?: "",
                            empleadoNombre = doc.getString("empleadoNombre") ?: "",
                            tipo = doc.getString("tipo") ?: "mesa",
                            mesaId = doc.getString("mesaId") ?: "",
                            mesaNumero = (doc.getLong("mesaNumero") ?: 0).toInt(),
                            clienteNombre = doc.getString("clienteNombre") ?: "",
                            estado = doc.getString("estado") ?: "pendiente",
                            items = items,
                            total = doc.getDouble("total") ?: 0.0,
                            fecha = doc.getTimestamp("fecha")
                        )
                    } ?: emptyList()

                // Ordenar por fecha descendente en memoria
                val ordenados = nuevos.sortedByDescending { it.fecha?.seconds }

                adapter.actualizar(ordenados)
                binding.tvContador.text = "${ordenados.size} activos"
                binding.layoutVacio.visibility =
                    if (ordenados.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun actualizarEstado(pedido: Pedido, nuevoEstado: String) {
        db.collection("pedidos").document(pedido.id)
            .update("estado", nuevoEstado)
            .addOnSuccessListener {
                // Si se paga, liberar la mesa
                if (nuevoEstado == "pagado" && pedido.mesaId.isNotEmpty()) {
                    db.collection("mesas").document(pedido.mesaId)
                        .update("estado", "libre")
                }
                val msg = if (nuevoEstado == "entregado") "Pedido entregado" else "Pago registrado"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}