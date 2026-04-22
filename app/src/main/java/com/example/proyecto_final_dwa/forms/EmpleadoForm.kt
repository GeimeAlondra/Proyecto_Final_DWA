package com.example.proyecto_final_dwa

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.FormEmpleadoBinding

class EmpleadoForm : AppCompatActivity() {

    private lateinit var binding: FormEmpleadoBinding
    private lateinit var db: FirebaseFirestore
    private var empleadoId: String? = null

    private val puestos = listOf(
        "Mesero", "Cocinero", "Cajero",
        "Bartender", "Host / Hostess", "Supervisor", "Gerente"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FormEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupPuestos()
        cargarDatosEdicion()

        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupPuestos() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, puestos)
        binding.spinnerPuesto.setAdapter(adapter)
    }

    private fun cargarDatosEdicion() {
        empleadoId = intent.getStringExtra("empleadoId")
        if (empleadoId != null) {
            binding.tvTituloForm.text = "Editar Empleado"
            binding.etNombre.setText(intent.getStringExtra("nombre"))
            binding.etEmail.setText(intent.getStringExtra("email"))
            binding.etTelefono.setText(intent.getStringExtra("telefono"))
            binding.spinnerPuesto.setText(intent.getStringExtra("puesto"), false)
            binding.switchActivo.isChecked = intent.getBooleanExtra("activo", true)
            // Email no editable en modo edición
            binding.etEmail.isEnabled = false
            binding.tilEmail.alpha = 0.6f
            binding.btnGuardar.text = "Actualizar Empleado"
        }
    }

    private fun guardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val puesto = binding.spinnerPuesto.text.toString().trim()
        val activo = binding.switchActivo.isChecked

        if (nombre.isEmpty()) {
            binding.tilNombre.error = "Ingresa el nombre"
            return
        } else binding.tilNombre.error = null

        if (email.isEmpty() && empleadoId == null) {
            binding.tilEmail.error = "Ingresa el correo"
            return
        } else binding.tilEmail.error = null

        if (puesto.isEmpty() || !puestos.contains(puesto)) {
            binding.tilPuesto.error = "Selecciona un puesto"
            return
        } else binding.tilPuesto.error = null

        setLoading(true)

        if (empleadoId == null) {
            // CREAR — guardar en Firestore con uid temporal
            // El empleado se registrará con su propia cuenta
            // Aquí solo guardamos el perfil adicional
            val datos = hashMapOf(
                "nombre" to nombre,
                "email" to email,
                "telefono" to telefono,
                "puesto" to puesto,
                "activo" to activo,
                "rol" to "empleado",
                "fechaRegistro" to com.google.firebase.Timestamp.now()
            )
            db.collection("usuarios").add(datos)
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(this, "Empleado agregado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    setLoading(false)
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // ACTUALIZAR
            val datos = mapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "puesto" to puesto,
                "activo" to activo
            )
            db.collection("usuarios").document(empleadoId!!)
                .update(datos)
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(this, "Empleado actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    setLoading(false)
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnGuardar.isEnabled = !isLoading
        binding.btnGuardar.text = if (isLoading) "Guardando..."
        else if (empleadoId == null) "Guardar Empleado" else "Actualizar Empleado"
    }
}