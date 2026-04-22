package com.example.proyecto_final_dwa

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.ActivityEmpleadoBinding

class EmpleadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpleadoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: EmpleadoAdapter
    private val listaTodos = mutableListOf<Empleado>()
    private val listaFiltrada = mutableListOf<Empleado>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupRecycler()
        cargarEmpleados()
        setupBuscador()

        binding.fabAgregar.setOnClickListener {
            startActivity(Intent(this, EmpleadoForm::class.java))
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecycler() {
        adapter = EmpleadoAdapter(
            listaFiltrada,
            onEditar = { empleado ->
                val intent = Intent(this, EmpleadoForm::class.java)
                intent.putExtra("empleadoId", empleado.id)
                intent.putExtra("nombre", empleado.nombre)
                intent.putExtra("email", empleado.email)
                intent.putExtra("telefono", empleado.telefono)
                intent.putExtra("puesto", empleado.puesto)
                intent.putExtra("activo", empleado.activo)
                startActivity(intent)
            },
            onEliminar = { empleado ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar empleado")
                    .setMessage("¿Eliminar a \"${empleado.nombre}\"? Esta acción no se puede deshacer.")
                    .setPositiveButton("Eliminar") { _, _ -> eliminarEmpleado(empleado) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerEmpleados.layoutManager = LinearLayoutManager(this)
        binding.recyclerEmpleados.adapter = adapter
    }

    private fun cargarEmpleados() {
        db.collection("usuarios")
            .whereEqualTo("rol", "empleado")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                listaTodos.clear()
                snapshot?.documents?.forEach { doc ->
                    listaTodos.add(
                        Empleado(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            email = doc.getString("email") ?: "",
                            telefono = doc.getString("telefono") ?: "",
                            puesto = doc.getString("puesto") ?: "",
                            activo = doc.getBoolean("activo") ?: true
                        )
                    )
                }

                // Aplicar búsqueda activa si hay texto
                val query = binding.etBuscar.text.toString()
                adapter.filtrar(query, listaTodos)

                binding.tvContador.text = "${listaTodos.size} empleados"
                binding.layoutVacio.visibility =
                    if (listaTodos.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun setupBuscador() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filtrar(s.toString(), listaTodos)
                binding.layoutVacio.visibility =
                    if (adapter.itemCount == 0) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun eliminarEmpleado(empleado: Empleado) {
        // Solo elimina el documento de Firestore
        // La cuenta de Authentication se mantiene (requeriría Admin SDK para borrarla)
        db.collection("usuarios").document(empleado.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "\"${empleado.nombre}\" eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}