package com.example.proyecto_final_dwa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_dwa.Pedido
import com.example.proyecto_final_dwa.databinding.ItemHistorialPedidosBinding
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialAdapter(
    private var lista: MutableList<Pedido>
) : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHistorialPedidosBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorialPedidosBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pedido = lista[position]
        with(holder.binding) {
            tvIdPedido.text = "Pedido #${pedido.id.take(6).uppercase()}"

            tvInfoAsignacion.text = if (pedido.tipo == "mesa")
                "Mesa ${pedido.mesaNumero}"
            else
                "Para llevar — ${pedido.clienteNombre}"

            tvItemsResumen.text = pedido.items.joinToString(", ") {
                "${it.cantidad}x ${it.nombre}"
            }

            tvTotal.text = "$%.2f".format(pedido.total)

            // Formatear fecha
            pedido.fecha?.let { timestamp ->
                val sdf = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale("es"))
                tvFecha.text = sdf.format(timestamp.toDate())
            } ?: run {
                tvFecha.text = "Fecha no disponible"
            }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizar(nuevaLista: List<Pedido>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}