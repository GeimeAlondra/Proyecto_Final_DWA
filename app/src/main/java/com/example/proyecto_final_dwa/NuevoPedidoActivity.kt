package com.example.proyecto_final_dwa

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto_final_dwa.databinding.ActivityNuevoPedidoBinding
import com.example.proyecto_final_dwa.models.Producto

class NuevoPedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoPedidoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val listaProductos = mutableListOf<Producto>()
    private val cantidades = mutableMapOf<String, Int>()
    private val listaMesas = mutableListOf<Mesa>()

    private lateinit var productosAdapter: ProductoPedidoAdapter
    private lateinit var resumenAdapter: ResumenAdapter

    private var tipoSeleccionado = "mesa"
    private var mesaSeleccionada: Mesa? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclers()
        cargarProductos()
        cargarMesas()
        setupTipoPedido()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnConfirmar.setOnClickListener { confirmarPedido() }
    }

    private fun setupRecyclers() {
        productosAdapter = ProductoPedidoAdapter(listaProductos, cantidades) {
            actualizarResumen()
        }
        binding.recyclerProductos.layoutManager = LinearLayoutManager(this)
        binding.recyclerProductos.adapter = productosAdapter

        resumenAdapter = ResumenAdapter(emptyList())
        binding.recyclerResumen.layoutManager = LinearLayoutManager(this)
        binding.recyclerResumen.adapter = resumenAdapter
    }

    private fun cargarProductos() {
        db.collection("productos")
            .whereEqualTo("disponible", true)
            .orderBy("nombre")
            .get()
            .addOnSuccessListener { snapshot ->
                listaProductos.clear()
                snapshot.documents.forEach { doc ->
                    listaProductos.add(
                        Producto(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            precio = doc.getDouble("precio") ?: 0.0,
                            categoria = doc.getString("categoria") ?: "",
                            disponible = true
                        )
                    )
                }
                productosAdapter.notifyDataSetChanged()
            }
    }

    private fun cargarMesas() {
        db.collection("mesas")
            .whereEqualTo("estado", "libre")
            .orderBy("numero")
            .get()
            .addOnSuccessListener { snapshot ->
                listaMesas.clear()
                snapshot.documents.forEach { doc ->
                    listaMesas.add(
                        Mesa(
                            id = doc.id,
                            numero = (doc.getLong("numero") ?: 0).toInt(),
                            capacidad = (doc.getLong("capacidad") ?: 0).toInt(),
                            estado = "libre"
                        )
                    )
                }
                val opciones = listaMesas.map { "Mesa ${it.numero} (cap. ${it.capacidad})" }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
                binding.spinnerMesa.setAdapter(adapter)

                binding.spinnerMesa.setOnItemClickListener { _, _, position, _ ->
                    mesaSeleccionada = listaMesas[position]
                }
            }
    }

    private fun setupTipoPedido() {
        // Mesa seleccionado por defecto
        seleccionarTipo("mesa")

        binding.btnTipoMesa.setOnClickListener { seleccionarTipo("mesa") }
        binding.btnTipoLlevar.setOnClickListener { seleccionarTipo("llevar") }
    }

    private fun seleccionarTipo(tipo: String) {
        tipoSeleccionado = tipo

        if (tipo == "mesa") {
            // Mesa activo
            binding.btnTipoMesa.setCardBackgroundColor(getColor(R.color.charcoal))
            binding.btnTipoMesa.strokeColor = getColor(R.color.gold)
            binding.btnTipoMesa.strokeWidth = 2
            binding.tvLabelMesa.setTextColor(getColor(R.color.gold))

            // Llevar inactivo
            binding.btnTipoLlevar.setCardBackgroundColor(getColor(R.color.white))
            binding.btnTipoLlevar.strokeWidth = 0
            binding.tvLabelLlevar.setTextColor(getColor(R.color.charcoal_light))

            binding.layoutSelectorMesa.visibility = View.VISIBLE
            binding.layoutNombreCliente.visibility = View.GONE
        } else {
            // Llevar activo
            binding.btnTipoLlevar.setCardBackgroundColor(getColor(R.color.charcoal))
            binding.btnTipoLlevar.strokeColor = getColor(R.color.gold)
            binding.btnTipoLlevar.strokeWidth = 2
            binding.tvLabelLlevar.setTextColor(getColor(R.color.gold))

            // Mesa inactivo
            binding.btnTipoMesa.setCardBackgroundColor(getColor(R.color.white))
            binding.btnTipoMesa.strokeWidth = 0
            binding.tvLabelMesa.setTextColor(getColor(R.color.charcoal_light))

            binding.layoutSelectorMesa.visibility = View.GONE
            binding.layoutNombreCliente.visibility = View.VISIBLE
        }
    }

    private fun actualizarResumen() {
        val items = listaProductos
            .filter { (cantidades[it.id] ?: 0) > 0 }
            .map { producto ->
                ItemPedido(
                    productoId = producto.id,
                    nombre = producto.nombre,
                    precio = producto.precio,
                    cantidad = cantidades[producto.id] ?: 0
                )
            }

        val total = items.sumOf { it.subtotal() }
        resumenAdapter.actualizar(items)
        binding.tvTotal.text = "$%.2f".format(total)
        binding.tvTotalFinal.text = "$%.2f".format(total)
    }

    private fun confirmarPedido() {
        val items = listaProductos
            .filter { (cantidades[it.id] ?: 0) > 0 }
            .map { producto ->
                ItemPedido(
                    productoId = producto.id,
                    nombre = producto.nombre,
                    precio = producto.precio,
                    cantidad = cantidades[producto.id] ?: 0
                )
            }

        if (items.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar mesa o cliente
        if (tipoSeleccionado == "mesa" && mesaSeleccionada == null) {
            binding.tilMesa.error = "Selecciona una mesa"
            return
        } else binding.tilMesa.error = null

        val clienteNombre = binding.etCliente.text.toString().trim()
        if (tipoSeleccionado == "llevar" && clienteNombre.isEmpty()) {
            binding.tilCliente.error = "Ingresa el nombre del cliente"
            return
        } else binding.tilCliente.error = null

        val total = items.sumOf { it.subtotal() }
        val uid = auth.currentUser?.uid ?: return

        binding.btnConfirmar.isEnabled = false
        binding.btnConfirmar.text = "Guardando..."

        // Obtener nombre del empleado
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val nombreEmpleado = doc.getString("nombre") ?: "Empleado"

                val itemsMap = items.map { item ->
                    hashMapOf(
                        "productoId" to item.productoId,
                        "nombre" to item.nombre,
                        "precio" to item.precio,
                        "cantidad" to item.cantidad
                    )
                }

                val pedido = hashMapOf(
                    "empleadoId" to uid,
                    "empleadoNombre" to nombreEmpleado,
                    "tipo" to tipoSeleccionado,
                    "mesaId" to (mesaSeleccionada?.id ?: ""),
                    "mesaNumero" to (mesaSeleccionada?.numero ?: 0),
                    "clienteNombre" to clienteNombre,
                    "estado" to "pendiente",
                    "items" to itemsMap,
                    "total" to total,
                    "fecha" to Timestamp.now()
                )

                db.collection("pedidos").add(pedido)
                    .addOnSuccessListener { ref ->
                        // Si es mesa, actualizar su estado a "ocupada"
                        mesaSeleccionada?.let { mesa ->
                            db.collection("mesas").document(mesa.id)
                                .update("estado", "ocupada")
                        }
                        Toast.makeText(this, "Pedido #${ref.id.take(6)} creado ✓", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        binding.btnConfirmar.isEnabled = true
                        binding.btnConfirmar.text = "Confirmar Pedido"
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
    }
}