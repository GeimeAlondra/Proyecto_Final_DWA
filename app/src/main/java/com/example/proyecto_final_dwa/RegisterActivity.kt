package com.example.proyecto_final_dwa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (!validateFields(nombre, email, password, confirmPassword)) return@setOnClickListener

            setLoading(true)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        guardarUsuario(uid, nombre, email)
                    } else {
                        setLoading(false)
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    private fun guardarUsuario(uid: String, nombre: String, email: String) {
        val usuario = hashMapOf(
            "uid" to uid,
            "nombre" to nombre,
            "email" to email,
            "rol" to "admin", // Todos los registros nuevos son admins
            "fechaRegistro" to com.google.firebase.Timestamp.now()
        )

        db.collection("usuarios").document(uid).set(usuario)
            .addOnSuccessListener {
                setLoading(false)
                startActivity(Intent(this, DashboardAdminActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun validateFields(nombre: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true
        if (nombre.isEmpty()) { binding.tilNombre.error = "Ingresa tu nombre"; isValid = false }
        else binding.tilNombre.error = null

        if (email.isEmpty()) { binding.tilEmail.error = "Ingresa tu correo"; isValid = false }
        else binding.tilEmail.error = null

        if (password.isEmpty()) { binding.tilPassword.error = "Ingresa una contraseña"; isValid = false }
        else if (password.length < 6) { binding.tilPassword.error = "Mínimo 6 caracteres"; isValid = false }
        else binding.tilPassword.error = null

        if (confirmPassword != password) { binding.tilConfirmPassword.error = "Las contraseñas no coinciden"; isValid = false }
        else binding.tilConfirmPassword.error = null

        return isValid
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = if (isLoading) "Creando cuenta..." else "Crear cuenta"
    }
}