package com.example.proyecto_final_dwa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.ActivityProductoBinding
import com.example.proyecto_final_dwa.models.Producto

class ProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ProductoAdapter
    private val listaProductos = mutableListOf<Producto>()
    private val listaTodos = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupRecycler()
        cargarProductos()
        setupBuscador()

        binding.fabAgregar.setOnClickListener {
            startActivity(Intent(this, ProductoForm::class.java))
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecycler() {
        adapter = ProductoAdapter(
            listaProductos,
            onEditar = { producto ->
                val intent = Intent(this, ProductoForm::class.java)
                intent.putExtra("productoId", producto.id)
                intent.putExtra("nombre", producto.nombre)
                intent.putExtra("descripcion", producto.descripcion)
                intent.putExtra("precio", producto.precio)
                intent.putExtra("categoria", producto.categoria)
                intent.putExtra("disponible", producto.disponible)
                startActivity(intent)
            },
            onEliminar = { producto ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar producto")
                    .setMessage("¿Eliminar \"${producto.nombre}\"? Esta acción no se puede deshacer.")
                    .setPositiveButton("Eliminar") { _, _ -> eliminarProducto(producto) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.recyclerProductos.layoutManager = LinearLayoutManager(this)
        binding.recyclerProductos.adapter = adapter
    }

    private fun cargarProductos() {
        db.collection("productos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                listaTodos.clear()
                snapshot?.documents?.forEach { doc ->
                    val disponible = doc.getBoolean("disponible") ?: true
                    listaTodos.add(
                        Producto(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            precio = doc.getDouble("precio") ?: 0.0,
                            categoria = doc.getString("categoria") ?: "",
                            disponible = disponible
                        )
                    )
                }
                listaTodos.sortBy { it.nombre }

                // Aplicar búsqueda activa si hay texto
                val query = binding.etBuscar.text.toString()
                adapter.filtrar(query, listaTodos)

                binding.tvContador.text = "${listaTodos.size} items"
                binding.layoutVacio.visibility =
                    if (listaTodos.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun eliminarProducto(producto: Producto) {
        db.collection("productos").document(producto.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "\"${producto.nombre}\" eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBuscador() {
        binding.etBuscar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filtrar(s.toString(), listaTodos)
                binding.layoutVacio.visibility =
                    if (adapter.itemCount == 0) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
    }
}