package com.example.proyecto_final_dwa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Si ya hay sesión activa, redirigir directo por rol
        auth.currentUser?.let {
            redirigirPorRol(it.uid)
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!validateFields(email, password)) return@setOnClickListener

            setLoading(true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        redirigirPorRol(uid)
                    } else {
                        setLoading(false)
                        Toast.makeText(
                            this,
                            "Credenciales incorrectas",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun redirigirPorRol(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                setLoading(false)
                when (doc.getString("rol")) {
                    "admin" -> {
                        startActivity(Intent(this, DashboardAdminActivity::class.java))
                        finish()
                    }
                    "empleado" -> {
                        startActivity(Intent(this, DashboardEmpleadoActivity::class.java))
                        finish()
                    }
                    else -> {
                        // Rol desconocido o documento no existe
                        auth.signOut()
                        Toast.makeText(
                            this,
                            "Usuario sin rol asignado. Contacta al administrador.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Error al obtener perfil: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun validateFields(email: String, password: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            binding.tilEmail.error = "Ingresa tu correo"
            isValid = false
        } else binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Ingresa tu contraseña"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Mínimo 6 caracteres"
            isValid = false
        } else binding.tilPassword.error = null

        return isValid
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "Verificando..." else "Ingresar"
    }
}