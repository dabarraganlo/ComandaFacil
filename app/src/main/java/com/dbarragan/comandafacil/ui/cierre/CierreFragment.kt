package com.dbarragan.comandafacil.ui.cierre

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dbarragan.comandafacil.R
import com.dbarragan.comandafacil.data.model.Jornada
import com.dbarragan.comandafacil.databinding.FragmentCierreBinding
import com.dbarragan.comandafacil.util.CurrencyFormatter
import com.dbarragan.comandafacil.util.DateHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CierreFragment : Fragment() {

    private var _binding: FragmentCierreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JornadaViewModel by viewModels()
    private val historialAdapter = HistorialAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCierreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorial.adapter = historialAdapter

        observarViewModel()

        binding.btnCerrarJornada.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirmar_cierre)
                .setMessage(R.string.mensaje_confirmar_cierre)
                .setPositiveButton(R.string.cerrar_jornada) { _, _ -> viewModel.cerrarJornada() }
                .setNegativeButton(R.string.cancelar, null)
                .show()
        }

        viewModel.cargar()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargar()
    }

    private fun observarViewModel() {
        viewModel.jornadaHoy.observe(viewLifecycleOwner) { jornada ->
            jornada ?: return@observe
            mostrarResumen(jornada)
            binding.btnCerrarJornada.isEnabled = !jornada.estaCerrada
            binding.btnCerrarJornada.text = if (jornada.estaCerrada)
                getString(R.string.jornada_ya_cerrada) else getString(R.string.cerrar_jornada)
        }

        viewModel.historial.observe(viewLifecycleOwner) { lista ->
            // Muestra historial excluyendo el día de hoy
            historialAdapter.submitList(lista.filter { it.fecha != DateHelper.hoy() })
        }

        viewModel.jornadaCerrada.observe(viewLifecycleOwner) { jornada ->
            jornada ?: return@observe
            val mensaje = if (jornada.metaAlcanzada)
                getString(R.string.mensaje_meta_alcanzada,
                    CurrencyFormatter.format(jornada.gananciaNeta))
            else
                getString(R.string.mensaje_meta_no_alcanzada,
                    CurrencyFormatter.format(jornada.gananciaNeta))
            binding.tvMensajeMeta.text = mensaje
            binding.tvMensajeMeta.visibility = View.VISIBLE
        }
    }

    private fun mostrarResumen(j: Jornada) {
        binding.tvFechaJornada.text = DateHelper.formatoDisplay(j.fecha)
        binding.tvIngresos.text     = CurrencyFormatter.format(j.totalIngresos)
        binding.tvCostos.text       = CurrencyFormatter.format(j.totalCostos)
        binding.tvGastos.text       = CurrencyFormatter.format(j.totalGastos)

        val ganancia = j.gananciaNeta
        binding.tvGananciaNeta.text = CurrencyFormatter.format(ganancia)
        binding.tvGananciaNeta.setTextColor(
            resources.getColor(
                if (ganancia >= 0) R.color.ganancia_positiva else R.color.ganancia_negativa, null
            )
        )
        binding.tvMargenReal.text = getString(R.string.margen_formato, j.margenReal)

        val progreso = j.progresoMeta.coerceAtMost(100)
        binding.progressBarCierre.progress = progreso
        binding.tvProgresoCierre.text = "$progreso%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Adapter historial ─────────────────────────────────────────────────
    class HistorialAdapter :
        androidx.recyclerview.widget.ListAdapter<Jornada,
        HistorialAdapter.ViewHolder>(DIFF) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_historial, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(getItem(position))

        inner class ViewHolder(view: View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            private val tvFecha    = view.findViewById<android.widget.TextView>(R.id.tvFechaHistorial)
            private val tvGanancia = view.findViewById<android.widget.TextView>(R.id.tvGananciaHistorial)
            private val tvEstado   = view.findViewById<android.widget.TextView>(R.id.tvEstadoHistorial)

            fun bind(j: Jornada) {
                tvFecha.text    = DateHelper.formatoDisplay(j.fecha)
                tvGanancia.text = CurrencyFormatter.format(j.gananciaNeta)
                tvEstado.text   = j.estado.replaceFirstChar { it.uppercase() }
                tvGanancia.setTextColor(
                    itemView.context.resources.getColor(
                        if (j.gananciaNeta >= 0) R.color.ganancia_positiva
                        else R.color.ganancia_negativa, null
                    )
                )
            }
        }

        companion object {
            val DIFF = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Jornada>() {
                override fun areItemsTheSame(a: Jornada, b: Jornada) = a.id == b.id
                override fun areContentsTheSame(a: Jornada, b: Jornada) = a == b
            }
        }
    }
}
