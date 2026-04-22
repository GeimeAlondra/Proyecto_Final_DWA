package com.example.proyecto_final_dwa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_dwa.databinding.ItemPedidoBinding

class PedidoAdapter(
    private var lista: MutableList<Pedido>,
    private val onEntregar: (Pedido) -> Unit,
    private val onPagar: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPedidoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPedidoBinding.inflate(
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

            tvTotalPedido.text = "$%.2f".format(pedido.total)

            // Estado visual
            val (textoEstado, badgeRes) = when (pedido.estado) {
                "entregado" -> "Entregado" to R.drawable.badge_verde
                "pagado"    -> "Pagado"    to R.drawable.badge_gold
                else        -> "Pendiente" to R.drawable.badge_rojo
            }
            tvEstadoPedido.text = textoEstado
            tvEstadoPedido.setBackgroundResource(badgeRes)

            // Visibilidad botones según estado
            btnEntregar.visibility = if (pedido.estado == "pendiente")
                android.view.View.VISIBLE else android.view.View.GONE
            btnPagar.visibility = if (pedido.estado == "entregado")
                android.view.View.VISIBLE else android.view.View.GONE

            btnEntregar.setOnClickListener { onEntregar(pedido) }
            btnPagar.setOnClickListener { onPagar(pedido) }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizar(nuevaLista: List<Pedido>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}