package com.example.proyecto_final_dwa

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.FormEmpleadoBinding
import com.google.firebase.FirebaseApp

class EmpleadoForm : AppCompatActivity() {

    private lateinit var binding: FormEmpleadoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var authPrincipal: FirebaseAuth  // Sesión del admin
    private var empleadoId: String? = null

    private val puestos = listOf(
        "Mesero", "Cocinero", "Cajero", "Bartender", "Supervisor", "Gerente"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FormEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        authPrincipal = FirebaseAuth.getInstance()

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
            binding.tvTituloForm.text = "Editar empleado"
            binding.etNombre.setText(intent.getStringExtra("nombre"))
            binding.etEmail.setText(intent.getStringExtra("email"))
            binding.etTelefono.setText(intent.getStringExtra("telefono"))
            binding.spinnerPuesto.setText(intent.getStringExtra("puesto"), false)
            binding.switchActivo.isChecked = intent.getBooleanExtra("activo", true)

            // Email y contraseña no se modifican
            binding.etEmail.isEnabled = false
            binding.tilEmail.alpha = 0.6f
            binding.etPassword.isEnabled = false
            binding.tilPassword.alpha = 0.6f
            binding.tilPassword.hint = "Contraseña (no editable)"

            binding.btnGuardar.text = "Actualizar Empleado"
        }
    }

    private fun guardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val puesto = binding.spinnerPuesto.text.toString().trim()
        val activo = binding.switchActivo.isChecked

        // Validaciones
        if (nombre.isEmpty()) {
            binding.tilNombre.error = "Ingresa el nombre"; return
        } else binding.tilNombre.error = null

        if (empleadoId == null) {
            if (email.isEmpty()) {
                binding.tilEmail.error = "Ingresa el correo"; return
            } else binding.tilEmail.error = null

            if (password.length < 6) {
                binding.tilPassword.error = "Mínimo 6 caracteres"; return
            } else binding.tilPassword.error = null
        }

        if (puesto.isEmpty() || !puestos.contains(puesto)) {
            binding.tilPuesto.error = "Selecciona un puesto"; return
        } else binding.tilPuesto.error = null

        setLoading(true)

        if (empleadoId == null) {
            crearEmpleadoConAuth(nombre, email, password, telefono, puesto, activo)
        } else {
            actualizarEmpleado(nombre, telefono, puesto, activo)
        }
    }

    private fun crearEmpleadoConAuth(
        nombre: String, email: String, password: String,
        telefono: String, puesto: String, activo: Boolean
    ) {
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

        authSecundario.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = authSecundario.currentUser!!.uid
                    authSecundario.signOut()

                    val datos = hashMapOf(
                        "uid" to uid,
                        "nombre" to nombre,
                        "email" to email,
                        "telefono" to telefono,
                        "puesto" to puesto,
                        "activo" to activo,
                        "rol" to "empleado",
                        "fechaRegistro" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("usuarios").document(uid).set(datos)
                        .addOnSuccessListener {
                            setLoading(false)
                            Toast.makeText(
                                this,
                                "Empleado creado\nEmail: $email\nContraseña: $password",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            setLoading(false)
                            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    setLoading(false)
                    Toast.makeText(
                        this,
                        "Error al crear cuenta: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun actualizarEmpleado(nombre: String, telefono: String, puesto: String, activo: Boolean) {
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
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnGuardar.isEnabled = !isLoading
        binding.btnGuardar.text = if (isLoading) "Guardando..."
        else if (empleadoId == null) "Guardar empleado" else "Actualizar empleado"
    }
}