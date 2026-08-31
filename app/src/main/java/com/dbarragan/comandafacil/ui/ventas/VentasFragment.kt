package com.dbarragan.comandafacil.ui.ventas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dbarragan.comandafacil.R
import com.dbarragan.comandafacil.data.model.DetalleVenta
import com.dbarragan.comandafacil.data.model.Producto
import com.dbarragan.comandafacil.databinding.FragmentVentasBinding
import com.dbarragan.comandafacil.util.CurrencyFormatter

class VentasFragment : Fragment() {

    private var _binding: FragmentVentasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VentasViewModel by viewModels()

    private var productoSeleccionado: Producto? = null
    private val adapterVentas = VentasAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVentasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvVentasHoy.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVentasHoy.adapter = adapterVentas
        observarViewModel()
        configurarSpinner()
        configurarCampos()
        viewModel.cargar()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargar()
    }

    private fun observarViewModel() {
        viewModel.productos.observe(viewLifecycleOwner) { lista ->
            val nombres = lista.map { it.nombre }
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerProducto.adapter = adapter

            binding.spinnerProducto.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        productoSeleccionado = lista.getOrNull(pos)
                        actualizarPrecioSugerido()
                    }
                    override fun onNothingSelected(p: AdapterView<*>?) {
                        productoSeleccionado = null
                    }
                }

            // Si aún no hay productos, se orienta al usuario en lugar de dejarlo sin respuesta
            binding.tvAyudaProducto.text =
                if (lista.isEmpty()) getString(R.string.error_sin_productos)
                else getString(R.string.ayuda_seleccionar_producto)
        }

        viewModel.ventasHoy.observe(viewLifecycleOwner) { ventas ->
            adapterVentas.submitList(ventas)
            binding.tvConteoVentas.text =
                getString(R.string.ventas_registradas_hoy, ventas.size)
        }

        viewModel.ventaRegistrada.observe(viewLifecycleOwner) { ok ->
            if (ok == true) {
                Toast.makeText(requireContext(), R.string.venta_registrada, Toast.LENGTH_SHORT).show()
                binding.etCantidad.setText("1")
                actualizarPrecioSugerido()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun configurarSpinner() {
        binding.tvAyudaProducto.text = getString(R.string.ayuda_seleccionar_producto)
    }

    private fun configurarCampos() {
        binding.btnRegistrarVenta.setOnClickListener {
            val producto = productoSeleccionado
            if (producto == null) {
                // Sin este aviso el botón no respondía y el usuario quedaba sin saber por qué
                val mensaje = if (viewModel.productos.value.isNullOrEmpty())
                    R.string.error_sin_productos else R.string.error_seleccionar_producto
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val cantidad  = binding.etCantidad.text.toString().toDoubleOrNull() ?: 0.0
            val precio    = binding.etPrecioVenta.text.toString().toDoubleOrNull() ?: 0.0

            if (cantidad <= 0) {
                binding.etCantidad.error = getString(R.string.error_cantidad)
                return@setOnClickListener
            }
            if (precio <= 0) {
                binding.etPrecioVenta.error = getString(R.string.error_precio)
                return@setOnClickListener
            }

            viewModel.registrarVenta(producto.id, cantidad, precio)
        }
    }

    private fun actualizarPrecioSugerido() {
        val producto = productoSeleccionado ?: return
        binding.etPrecioVenta.setText(producto.precioSugerido.toInt().toString())
        binding.tvGananciaEstimada.text = getString(
            R.string.ganancia_estimada,
            CurrencyFormatter.format(producto.precioSugerido - producto.costoTotal)
        )
        binding.tvCostoProducto.text = getString(
            R.string.costo_producto,
            CurrencyFormatter.format(producto.costoTotal)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Adapter interno ───────────────────────────────────────────────────
    class VentasAdapter :
        androidx.recyclerview.widget.ListAdapter<DetalleVenta,
        VentasAdapter.ViewHolder>(DIFF) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_venta, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(getItem(position))

        inner class ViewHolder(view: View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            private val tvNombre   = view.findViewById<android.widget.TextView>(R.id.tvNombreVenta)
            private val tvCantidad = view.findViewById<android.widget.TextView>(R.id.tvCantidadVenta)
            private val tvTotal    = view.findViewById<android.widget.TextView>(R.id.tvTotalVenta)

            fun bind(d: DetalleVenta) {
                tvNombre.text   = d.nombreProducto
                tvCantidad.text = "x${d.cantidad.toInt()}"
                tvTotal.text    = CurrencyFormatter.format(d.subtotalIngreso)
            }
        }

        companion object {
            val DIFF = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<DetalleVenta>() {
                override fun areItemsTheSame(a: DetalleVenta, b: DetalleVenta) = a.id == b.id
                override fun areContentsTheSame(a: DetalleVenta, b: DetalleVenta) = a == b
            }
        }
    }
}
