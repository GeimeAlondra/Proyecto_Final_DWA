package com.example.proyecto_final_dwa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.ActivityDashboardEmpleadoBinding

class DashboardEmpleadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardEmpleadoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        cargarNombre()
        cargarContadorPedidos()
        setupCards()
        setupLogout()
    }

    private fun cargarNombre() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: "Empleado"
                val hora = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                binding.tvSaludo.text = when {
                    hora < 12 -> "Buenos días,"
                    hora < 18 -> "Buenas tardes,"
                    else      -> "Buenas noches,"
                }
                binding.tvNombre.text = nombre
            }
    }

    private fun cargarContadorPedidos() {
        val uid = auth.currentUser?.uid ?: return
        // Cuenta pedidos activos asignados a este empleado
        db.collection("pedidos")
            .whereEqualTo("empleadoId", uid)
            .whereEqualTo("estado", "pendiente")
            .get()
            .addOnSuccessListener { docs ->
                binding.tvContadorPedidos.text = docs.size().toString()
            }
    }

    private fun setupCards() {
        binding.cardNuevoPedido.setOnClickListener {
            // startActivity(Intent(this, NuevoPedidoActivity::class.java))
        }
        binding.cardPedidosActivos.setOnClickListener {
            // startActivity(Intent(this, PedidosActivosActivity::class.java))
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