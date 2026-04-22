package com.example.proyecto_final_dwa

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.FormMesaBinding

class MesaForm : AppCompatActivity() {

    private lateinit var binding: FormMesaBinding
    private lateinit var db: FirebaseFirestore
    private var mesaId: String? = null

    private val estados = listOf("libre", "ocupada", "reservada")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FormMesaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupEstados()
        cargarDatosEdicion()

        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupEstados() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados)
        binding.spinnerEstado.setAdapter(adapter)
        binding.spinnerEstado.setText("libre", false) // default
    }

    private fun cargarDatosEdicion() {
        mesaId = intent.getStringExtra("mesaId")
        if (mesaId != null) {
            binding.tvTituloForm.text = "Editar Mesa"
            binding.etNumero.setText(intent.getIntExtra("numero", 0).toString())
            binding.etCapacidad.setText(intent.getIntExtra("capacidad", 0).toString())
            binding.spinnerEstado.setText(intent.getStringExtra("estado"), false)
            binding.btnGuardar.text = "Actualizar Mesa"
        }
    }

    private fun guardar() {
        val numeroStr = binding.etNumero.text.toString().trim()
        val capacidadStr = binding.etCapacidad.text.toString().trim()
        val estado = binding.spinnerEstado.text.toString().trim()

        if (numeroStr.isEmpty()) {
            binding.tilNumero.error = "Ingresa el número"
            return
        } else binding.tilNumero.error = null

        val numero = numeroStr.toIntOrNull()
        if (numero == null || numero <= 0) {
            binding.tilNumero.error = "Número inválido"
            return
        } else binding.tilNumero.error = null

        if (capacidadStr.isEmpty()) {
            binding.tilCapacidad.error = "Ingresa la capacidad"
            return
        } else binding.tilCapacidad.error = null

        val capacidad = capacidadStr.toIntOrNull()
        if (capacidad == null || capacidad <= 0) {
            binding.tilCapacidad.error = "Capacidad inválida"
            return
        } else binding.tilCapacidad.error = null

        if (estado.isEmpty() || !estados.contains(estado)) {
            binding.tilEstado.error = "Selecciona un estado"
            return
        } else binding.tilEstado.error = null

        setLoading(true)

        val datos = hashMapOf(
            "numero" to numero,
            "capacidad" to capacidad,
            "estado" to estado
        )

        if (mesaId == null) {
            // Verificar que el número no esté repetido
            db.collection("mesas")
                .whereEqualTo("numero", numero)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        setLoading(false)
                        binding.tilNumero.error = "Ya existe una Mesa $numero"
                    } else {
                        db.collection("mesas").add(datos)
                            .addOnSuccessListener {
                                setLoading(false)
                                Toast.makeText(this, "Mesa $numero creada ✓", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                setLoading(false)
                                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
        } else {
            db.collection("mesas").document(mesaId!!)
                .update(datos as Map<String, Any>)
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(this, "Mesa actualizada ✓", Toast.LENGTH_SHORT).show()
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
        else if (mesaId == null) "Guardar Mesa" else "Actualizar Mesa"
    }
}