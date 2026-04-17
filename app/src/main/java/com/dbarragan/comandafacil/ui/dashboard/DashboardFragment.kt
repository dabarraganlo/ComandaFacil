package com.dbarragan.comandafacil.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dbarragan.comandafacil.R
import com.dbarragan.comandafacil.databinding.FragmentDashboardBinding
import com.dbarragan.comandafacil.util.CurrencyFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observarViewModel()
        configurarBotones()
        viewModel.cargar()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargar()
    }

    private fun observarViewModel() {
        viewModel.jornada.observe(viewLifecycleOwner) { jornada ->
            if (jornada == null) return@observe

            binding.tvEstadoJornada.text = if (jornada.estaCerrada)
                getString(R.string.jornada_cerrada) else getString(R.string.jornada_abierta)

            binding.tvTotalIngresos.text = CurrencyFormatter.format(jornada.totalIngresos)
            binding.tvTotalCostos.text   = CurrencyFormatter.format(jornada.totalCostos)
            binding.tvTotalGastos.text   = CurrencyFormatter.format(jornada.totalGastos)

            val ganancia = jornada.gananciaNeta
            binding.tvGananciaNeta.text  = CurrencyFormatter.format(ganancia)
            binding.tvGananciaNeta.setTextColor(
                resources.getColor(
                    if (ganancia >= 0) R.color.ganancia_positiva else R.color.ganancia_negativa,
                    null
                )
            )

            val progreso = jornada.progresoMeta.coerceAtMost(100)
            binding.progressBarMeta.progress = progreso
            binding.tvProgresoPorcentaje.text = "$progreso%"

            val cerrada = jornada.estaCerrada
            binding.btnRegistrarVenta.isEnabled  = !cerrada
            binding.btnRegistrarGasto.isEnabled  = !cerrada
            binding.btnCerrarJornada.isEnabled   = !cerrada
        }

        viewModel.productosConStockBajo.observe(viewLifecycleOwner) { lista ->
            if (lista.isEmpty()) {
                binding.tvAlertaStock.visibility = View.GONE
            } else {
                binding.tvAlertaStock.visibility = View.VISIBLE
                val nombres = lista.joinToString(", ") { it.nombre }
                binding.tvAlertaStock.text = getString(R.string.alerta_stock, nombres)
            }
        }

        viewModel.gastoRegistrado.observe(viewLifecycleOwner) { registrado ->
            if (registrado == true) viewModel.cargar()
        }
    }

    private fun configurarBotones() {
        binding.btnRegistrarVenta.setOnClickListener {
            findNavController().navigate(R.id.navigation_ventas)
        }

        binding.btnRegistrarGasto.setOnClickListener {
            mostrarDialogoGasto()
        }

        binding.btnCerrarJornada.setOnClickListener {
            findNavController().navigate(R.id.navigation_cierre)
        }
    }

    private fun mostrarDialogoGasto() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_gasto, null)
        val etDescripcion = dialogView.findViewById<TextInputEditText>(R.id.etDescripcionGasto)
        val etMonto       = dialogView.findViewById<TextInputEditText>(R.id.etMontoGasto)
        val spinnerTipo   = dialogView.findViewById<Spinner>(R.id.spinnerTipoGasto)

        ArrayAdapter.createFromResource(
            requireContext(), R.array.tipos_gasto, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTipo.adapter = adapter
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.registrar_gasto)
            .setView(dialogView)
            .setPositiveButton(R.string.guardar) { _, _ ->
                val desc  = etDescripcion.text.toString().trim()
                val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0
                val tipo  = if (spinnerTipo.selectedItemPosition == 0) "fijo" else "variable"
                if (desc.isNotEmpty() && monto > 0) {
                    viewModel.registrarGasto(desc, monto, tipo)
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
