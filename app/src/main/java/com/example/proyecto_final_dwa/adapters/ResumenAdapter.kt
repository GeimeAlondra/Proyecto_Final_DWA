package com.example.proyecto_final_dwa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_dwa.databinding.ItemResumenPedidoBinding

class ResumenAdapter(
    private var items: List<ItemPedido>
) : RecyclerView.Adapter<ResumenAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemResumenPedidoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResumenPedidoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvResumenCantidad.text = "${item.cantidad}x"
            tvResumenNombre.text = item.nombre
            tvResumenSubtotal.text = "$%.2f".format(item.subtotal())
        }
    }

    override fun getItemCount() = items.size

    fun actualizar(nuevos: List<ItemPedido>) {
        items = nuevos
        notifyDataSetChanged()
    }
}