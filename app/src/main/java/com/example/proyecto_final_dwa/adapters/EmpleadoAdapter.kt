package com.example.proyecto_final_dwa

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_dwa.databinding.ItemEmpleadoBinding

class EmpleadoAdapter(
    private var lista: MutableList<Empleado>,
    private val onEditar: (Empleado) -> Unit,
    private val onEliminar: (Empleado) -> Unit
) : RecyclerView.Adapter<EmpleadoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemEmpleadoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEmpleadoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val empleado = lista[position]
        with(holder.binding) {

            // Inicial del nombre como avatar
            tvAvatar.text = empleado.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            tvNombre.text = empleado.nombre
            tvPuesto.text = empleado.puesto
            tvEmail.text = empleado.email
            tvTelefono.text = if (empleado.telefono.isNotEmpty())
                "${empleado.telefono}" else "Sin teléfono"

            // Estado activo
            if (empleado.activo) {
                tvActivo.text = "Activo"
                tvActivo.setBackgroundResource(R.drawable.badge_gold)
                tvActivo.setTextColor(root.context.getColor(R.color.charcoal))
            } else {
                tvActivo.text = "Inactivo"
                tvActivo.setBackgroundResource(R.drawable.badge_gris)
                tvActivo.setTextColor(root.context.getColor(R.color.white))
            }

            // Color avatar según estado
            tvAvatar.setBackgroundResource(
                if (empleado.activo) R.drawable.circle_gold else R.drawable.circle_gris
            )

            btnOpciones.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add(0, 1, 0, "Editar")
                popup.menu.add(0, 2, 1, "Eliminar")
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> onEditar(empleado)
                        2 -> onEliminar(empleado)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizar(nuevaLista: List<Empleado>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun filtrar(query: String, listaOriginal: List<Empleado>) {
        val resultado = if (query.isEmpty()) listaOriginal
        else listaOriginal.filter {
            it.nombre.contains(query, ignoreCase = true) ||
                    it.puesto.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        }
        lista.clear()
        lista.addAll(resultado)
        notifyDataSetChanged()
    }
}