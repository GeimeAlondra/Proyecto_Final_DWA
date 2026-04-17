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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupRecycler()
        cargarProductos()

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
            .orderBy("nombre")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val nuevaLista = snapshot?.documents?.map { doc ->
                    Producto(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        precio = doc.getDouble("precio") ?: 0.0,
                        categoria = doc.getString("categoria") ?: "",
                        disponible = doc.getBoolean("disponible") ?: true
                    )
                } ?: emptyList()

                adapter.actualizar(nuevaLista)

                // Estado vacío
                binding.tvContador.text = "${nuevaLista.size} items"
                binding.layoutVacio.visibility =
                    if (nuevaLista.isEmpty()) View.VISIBLE else View.GONE
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

    override fun onResume() {
        super.onResume()
    }
}