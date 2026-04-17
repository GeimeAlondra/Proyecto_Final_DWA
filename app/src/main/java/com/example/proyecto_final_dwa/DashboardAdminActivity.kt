package com.example.proyecto_final_dwa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.ActivityDashboardAdminBinding

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        cargarNombre()
        setupCards()
        setupLogout()
    }

    private fun cargarNombre() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: "Admin"
                val hora = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                binding.tvSaludo.text = when {
                    hora < 12 -> "Buenos días,"
                    hora < 18 -> "Buenas tardes,"
                    else      -> "Buenas noches,"
                }
                binding.tvNombre.text = nombre
            }
    }

    private fun setupCards() {
        binding.cardProductos.setOnClickListener {
            binding.cardProductos.setOnClickListener {
                startActivity(Intent(this, ProductoActivity::class.java))
            }
        }
        binding.cardMesas.setOnClickListener {
            // startActivity(Intent(this, MesasActivity::class.java))
            Toast.makeText(this, "Próximamente: Mesas", Toast.LENGTH_SHORT).show()
        }
        binding.cardEmpleados.setOnClickListener {
            // startActivity(Intent(this, EmpleadosActivity::class.java))
            Toast.makeText(this, "Próximamente: Empleados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas salir del sistema?")
                .setPositiveButton("Salir") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

}