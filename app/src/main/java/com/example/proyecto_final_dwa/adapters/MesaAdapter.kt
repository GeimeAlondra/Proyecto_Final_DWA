package com.example.proyecto_final_dwa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_dwa.databinding.ItemMesaBinding

class MesaAdapter(
    private var lista: MutableList<Mesa>,
    private val onEditar: (Mesa) -> Unit,
    private val onEliminar: (Mesa) -> Unit
) : RecyclerView.Adapter<MesaAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMesaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMesaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mesa = lista[position]
        with(holder.binding) {

            tvNumeroMesa.text = mesa.numero.toString()
            tvCapacidad.text = "${mesa.capacidad}"

            // Color y texto según estado
            val (textoEstado, colorFondo, colorTexto) = when (mesa.estado) {
                "ocupada"   -> Triple("Ocupada",   R.color.error_red,      R.color.white)
                else        -> Triple("Libre",     R.color.gold,           R.color.white)
            }

            tvEstado.text = textoEstado
            tvEstado.setBackgroundResource(
                when (mesa.estado) {
                    "ocupada"   -> R.drawable.badge_rojo
                    else        -> R.drawable.badge_gold
                }
            )
            tvEstado.setTextColor(root.context.getColor(colorTexto))

            // Color del círculo del número
            tvNumeroMesa.setBackgroundResource(
                when (mesa.estado) {
                    "ocupada"   -> R.drawable.circle_rojo
                    else        -> R.drawable.circle_gold
                }
            )

            btnEditar.setOnClickListener { onEditar(mesa) }
            btnEliminar.setOnClickListener { onEliminar(mesa) }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizar(nuevaLista: List<Mesa>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}