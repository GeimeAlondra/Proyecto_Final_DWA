package com.example.proyecto_final_dwa

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_final_dwa.adapters.HistorialAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.proyecto_final_dwa.databinding.ActivityHistorialPedidosBinding

class HistorialPedidosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialPedidosBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: HistorialAdapter
    private val listaHistorial = mutableListOf<Pedido>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialPedidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecycler()
        cargarHistorial()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecycler() {
        adapter = HistorialAdapter(listaHistorial)
        binding.recyclerHistorial.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistorial.adapter = adapter
    }

    private fun cargarHistorial() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("pedidos")
            .whereEqualTo("empleadoId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val pagados = snapshot?.documents
                    ?.filter { it.getString("estado") == "pagado" }
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
                            tipo = doc.getString("tipo") ?: "mesa",
                            mesaId = doc.getString("mesaId") ?: "",
                            mesaNumero = (doc.getLong("mesaNumero") ?: 0).toInt(),
                            clienteNombre = doc.getString("clienteNombre") ?: "",
                            estado = "pagado",
                            items = items,
                            total = doc.getDouble("total") ?: 0.0,
                            fecha = doc.getTimestamp("fecha")
                        )
                    }
                    ?.sortedByDescending { it.fecha?.seconds }
                    ?: emptyList()

                adapter.actualizar(pagados)
                binding.tvContador.text = "${pagados.size} pedidos"
                binding.layoutVacio.visibility =
                    if (pagados.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}