package com.example.proyecto_final_dwa

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_dwa.databinding.ItemProductoBinding
import com.example.proyecto_final_dwa.models.Producto

class ProductoAdapter(
    private var lista: MutableList<Producto>,
    private val onEditar: (Producto) -> Unit,
    private val onEliminar: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = lista[position]
        with(holder.binding) {

            tvNombre.text = producto.nombre
            tvDescripcion.text = producto.descripcion.ifEmpty { "Sin descripción" }
            tvPrecio.text = "$%.2f".format(producto.precio)
            tvCategoria.text = producto.categoria

            val color = if (producto.disponible)
                root.context.getColor(R.color.gold)
            else
                root.context.getColor(R.color.warm_gray)
            viewDisponible.setBackgroundColor(color)

            tvDisponible.text = if (producto.disponible) "✓ Disponible" else "✗ No disponible"
            tvDisponible.setTextColor(color)


            btnOpciones.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add(0, 1, 0, "Editar")
                popup.menu.add(0, 2, 1, "Eliminar")
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> onEditar(producto)
                        2 -> onEliminar(producto)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizar(nuevaLista: List<Producto>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}