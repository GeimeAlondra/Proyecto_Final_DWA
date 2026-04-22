package com.example.proyecto_final_dwa

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.FormProductoBinding

class ProductoForm : AppCompatActivity() {

    private lateinit var binding: FormProductoBinding
    private lateinit var db: FirebaseFirestore
    private var productoId: String? = null

    private val categorias = listOf(
        "Entradas", "Sopas", "Platos fuertes", "Postres",
        "Bebidas", "Bebidas alcohólicas", "Extras"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FormProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupCategorias()
        cargarDatosEdicion()

        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupCategorias() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categorias)
        binding.spinnerCategoria.setAdapter(adapter)
    }

    private fun cargarDatosEdicion() {
        productoId = intent.getStringExtra("productoId")

        if (productoId != null) {
            binding.tvTituloForm.text = "Editar Producto"
            binding.etNombre.setText(intent.getStringExtra("nombre"))
            binding.etDescripcion.setText(intent.getStringExtra("descripcion"))
            binding.etPrecio.setText(intent.getDoubleExtra("precio", 0.0).toString())
            binding.spinnerCategoria.setText(intent.getStringExtra("categoria"), false)
            binding.switchDisponible.isChecked = intent.getBooleanExtra("disponible", true)
            binding.btnGuardar.text = "Actualizar Producto"
        }
    }

    private fun guardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val precioStr = binding.etPrecio.text.toString().trim()
        val categoria = binding.spinnerCategoria.text.toString().trim()
        val disponible = binding.switchDisponible.isChecked

        // Validaciones
        if (nombre.isEmpty()) {
            binding.tilNombre.error = "Ingresa el nombre"
            return
        } else binding.tilNombre.error = null

        if (precioStr.isEmpty()) {
            binding.tilPrecio.error = "Ingresa el precio"
            return
        } else binding.tilPrecio.error = null

        val precio = precioStr.toDoubleOrNull()
        if (precio == null || precio <= 0) {
            binding.tilPrecio.error = "Precio inválido"
            return
        } else binding.tilPrecio.error = null

        if (categoria.isEmpty() || !categorias.contains(categoria)) {
            binding.tilCategoria.error = "Selecciona una categoría"
            return
        } else binding.tilCategoria.error = null

        setLoading(true)

        val datos = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "precio" to precio,
            "categoria" to categoria,
            "disponible" to disponible
        )

        if (productoId == null) {
            // Agregar
            db.collection("productos").add(datos)
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    setLoading(false)
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // Actualizar
            db.collection("productos").document(productoId!!)
                .update(datos as Map<String, Any>)
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
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
        binding.btnGuardar.text = if (isLoading) "Guardando..." else
            if (productoId == null) "Guardar Producto" else "Actualizar Producto"
    }
}