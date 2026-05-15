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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

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
                intent.putExtra("password", empleado.password)
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
                            activo = doc.getBoolean("activo") ?: true,
                            password = doc.getString("password") ?: ""
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
        // Primero obtener la contraseña actual de Firestore
        db.collection("usuarios").document(empleado.id)
            .get()
            .addOnSuccessListener { doc ->
                val password = doc.getString("password") ?: ""

                if (password.isEmpty()) {
                    // Si no tiene contraseña guardada, solo eliminar de Firestore
                    eliminarSoloDeFirestore(empleado)
                    return@addOnSuccessListener
                }

                val secondaryApp = try {
                    FirebaseApp.getInstance("secondary")
                } catch (e: Exception) {
                    FirebaseApp.initializeApp(
                        this,
                        FirebaseApp.getInstance().options,
                        "secondary"
                    )
                }

                val authSecundario = FirebaseAuth.getInstance(secondaryApp!!)

                // Autenticar al empleado en instancia secundaria
                authSecundario.signInWithEmailAndPassword(empleado.email, password)
                    .addOnSuccessListener {
                        // Eliminar cuenta de Authentication
                        authSecundario.currentUser!!.delete()
                            .addOnSuccessListener {
                                authSecundario.signOut()
                                // Después eliminar de Firestore
                                eliminarSoloDeFirestore(empleado)
                            }
                            .addOnFailureListener { e ->
                                authSecundario.signOut()
                                Toast.makeText(
                                    this,
                                    "Error al eliminar cuenta: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        // Si falla la autenticación igual eliminar de Firestore
                        Toast.makeText(
                            this,
                            "No se pudo eliminar la cuenta de acceso: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        eliminarSoloDeFirestore(empleado)
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun eliminarSoloDeFirestore(empleado: Empleado) {
        db.collection("usuarios").document(empleado.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "\"${empleado.nombre}\" eliminado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}