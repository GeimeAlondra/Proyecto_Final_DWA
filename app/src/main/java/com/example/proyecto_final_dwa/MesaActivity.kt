package com.example.proyecto_final_dwa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.ActivityMesaBinding

class MesaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMesaBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MesaAdapter
    private val listaTodas = mutableListOf<Mesa>()
    private val listaFiltrada = mutableListOf<Mesa>()
    private var filtroActual = "todas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMesaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupRecycler()
        cargarMesas()
        setupFiltros()

        binding.fabAgregar.setOnClickListener {
            startActivity(Intent(this, MesaForm::class.java))
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecycler() {
        adapter = MesaAdapter(
            listaFiltrada,
            onEditar = { mesa ->
                val intent = Intent(this, MesaForm::class.java)
                intent.putExtra("mesaId", mesa.id)
                intent.putExtra("numero", mesa.numero)
                intent.putExtra("capacidad", mesa.capacidad)
                intent.putExtra("estado", mesa.estado)
                startActivity(intent)
            },
            onEliminar = { mesa ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar mesa")
                    .setMessage("¿Eliminar Mesa ${mesa.numero}?")
                    .setPositiveButton("Eliminar") { _, _ -> eliminarMesa(mesa) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerMesas.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerMesas.adapter = adapter
    }

    private fun cargarMesas() {
        db.collection("mesas")
            .orderBy("numero")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                listaTodas.clear()
                snapshot?.documents?.forEach { doc ->
                    listaTodas.add(
                        Mesa(
                            id = doc.id,
                            numero = (doc.getLong("numero") ?: 0).toInt(),
                            capacidad = (doc.getLong("capacidad") ?: 0).toInt(),
                            estado = doc.getString("estado") ?: "libre"
                        )
                    )
                }
                aplicarFiltro()
            }
    }

    private fun setupFiltros() {
        binding.chipTodas.setOnClickListener { filtroActual = "todas"; aplicarFiltro() }
        binding.chipLibres.setOnClickListener { filtroActual = "libre"; aplicarFiltro() }
        binding.chipOcupadas.setOnClickListener { filtroActual = "ocupada"; aplicarFiltro() }
    }

    private fun aplicarFiltro() {
        val resultado = if (filtroActual == "todas") listaTodas
        else listaTodas.filter { it.estado == filtroActual }

        listaFiltrada.clear()
        listaFiltrada.addAll(resultado)
        adapter.notifyDataSetChanged()

        binding.tvContador.text = "${listaTodas.size} mesas"
        binding.layoutVacio.visibility =
            if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun eliminarMesa(mesa: Mesa) {
        db.collection("mesas").document(mesa.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Mesa ${mesa.numero} eliminada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}