package com.example.proyecto_final_dwa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_dwa.databinding.ItemProductoPedidoBinding
import com.example.proyecto_final_dwa.models.Producto

class ProductoPedidoAdapter(
    private val lista: List<Producto>,
    private val cantidades: MutableMap<String, Int>,
    private val onCambio: () -> Unit
) : RecyclerView.Adapter<ProductoPedidoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProductoPedidoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductoPedidoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = lista[position]
        val cantidad = cantidades[producto.id] ?: 0

        with(holder.binding) {
            tvNombreProducto.text = producto.nombre
            tvPrecioProducto.text = "$%.2f".format(producto.precio)
            tvCantidad.text = cantidad.toString()

            // Resaltar si tiene cantidad > 0
            root.strokeWidth = if (cantidad > 0) 2 else 0
            root.strokeColor = root.context.getColor(
                if (cantidad > 0) R.color.gold else R.color.cream_dark
            )

            btnMas.setOnClickListener {
                cantidades[producto.id] = (cantidades[producto.id] ?: 0) + 1
                tvCantidad.text = cantidades[producto.id].toString()
                root.strokeWidth = 2
                root.strokeColor = root.context.getColor(R.color.gold)
                onCambio()
            }

            btnMenos.setOnClickListener {
                val actual = cantidades[producto.id] ?: 0
                if (actual > 0) {
                    cantidades[producto.id] = actual - 1
                    tvCantidad.text = cantidades[producto.id].toString()
                    if (cantidades[producto.id] == 0) {
                        root.strokeWidth = 0
                    }
                    onCambio()
                }
            }
        }
    }

    override fun getItemCount() = lista.size
}