package com.dbarragan.comandafacil.ui.productos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dbarragan.comandafacil.R
import com.dbarragan.comandafacil.data.model.Producto
import com.dbarragan.comandafacil.databinding.FragmentProductosBinding
import com.dbarragan.comandafacil.util.CurrencyFormatter
import com.dbarragan.comandafacil.util.DateHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class ProductosFragment : Fragment() {

    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductosViewModel by viewModels()
    private val adapter = ProductosAdapter { producto -> mostrarDialogoProducto(producto) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvProductos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProductos.adapter = adapter

        viewModel.productos.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
            binding.tvSinProductos.visibility =
                if (lista.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.guardado.observe(viewLifecycleOwner) { if (it == true) viewModel.cargar() }

        binding.fabAgregarProducto.setOnClickListener { mostrarDialogoProducto(null) }
        viewModel.cargar()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargar()
    }

    private fun mostrarDialogoProducto(productoExistente: Producto?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_agregar_producto, null)

        val etNombre        = dialogView.findViewById<TextInputEditText>(R.id.etNombre)
        val etCostoIng      = dialogView.findViewById<TextInputEditText>(R.id.etCostoIngredientes)
        val etCostoFijo     = dialogView.findViewById<TextInputEditText>(R.id.etCostoFijo)
        val etMargen        = dialogView.findViewById<TextInputEditText>(R.id.etMargenGanancia)
        val tvPrecioCalc    = dialogView.findViewById<android.widget.TextView>(R.id.tvPrecioCalculado)
        val etStockActual   = dialogView.findViewById<TextInputEditText>(R.id.etStockActual)
        val etStockMinimo   = dialogView.findViewById<TextInputEditText>(R.id.etStockMinimo)

        productoExistente?.let {
            etNombre.setText(it.nombre)
            etCostoIng.setText(it.costoIngredientes.toString())
            etCostoFijo.setText(it.costoFijoProrrateado.toString())
            etMargen.setText(it.margenGanancia.toString())
            etStockActual.setText(it.stockActual.toString())
            etStockMinimo.setText(it.stockMinimo.toString())
        }

        fun actualizarPrecio() {
            val costoIng  = etCostoIng.text.toString().toDoubleOrNull() ?: 0.0
            val costoFijo = etCostoFijo.text.toString().toDoubleOrNull() ?: 0.0
            val margen    = etMargen.text.toString().toDoubleOrNull() ?: 0.0
            if (margen >= 100.0) {
                // Se avisa mientras escribe, sin esperar a que intente guardar
                tvPrecioCalc.text = getString(R.string.ayuda_margen)
                etMargen.error = getString(R.string.error_margen)
                return
            }
            etMargen.error = null
            val temp = Producto("_temp", costoIng, costoFijo, margen)
            tvPrecioCalc.text = getString(R.string.precio_calculado,
                CurrencyFormatter.format(temp.precioSugerido))
        }

        etCostoIng.addTextChangedListener  { actualizarPrecio() }
        etCostoFijo.addTextChangedListener { actualizarPrecio() }
        etMargen.addTextChangedListener    { actualizarPrecio() }
        actualizarPrecio()

        val titulo = if (productoExistente == null) R.string.nuevo_producto else R.string.editar_producto
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setView(dialogView)
            // El listener se asigna despues de mostrar el dialogo para poder
            // mantenerlo abierto cuando la validacion no pasa
            .setPositiveButton(R.string.guardar, null)
            .setNegativeButton(R.string.cancelar, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = etNombre.text.toString().trim()
                val margen = etMargen.text.toString().toDoubleOrNull() ?: 30.0

                if (nombre.isEmpty()) {
                    etNombre.error = getString(R.string.error_nombre)
                    etNombre.requestFocus()
                    return@setOnClickListener
                }
                // Un margen de 100 o mas anula el divisor de la formula de precio
                if (margen >= 100.0 || margen < 0.0) {
                    etMargen.error = getString(R.string.error_margen)
                    etMargen.requestFocus()
                    return@setOnClickListener
                }

                val producto = Producto(
                    id                   = productoExistente?.id ?: 0,
                    nombre               = nombre,
                    costoIngredientes    = etCostoIng.text.toString().toDoubleOrNull() ?: 0.0,
                    costoFijoProrrateado = etCostoFijo.text.toString().toDoubleOrNull() ?: 0.0,
                    margenGanancia       = margen,
                    stockActual          = etStockActual.text.toString().toDoubleOrNull() ?: 0.0,
                    stockMinimo          = etStockMinimo.text.toString().toDoubleOrNull() ?: 0.0,
                    fechaCreacion        = productoExistente?.fechaCreacion ?: DateHelper.ahora()
                )
                viewModel.guardar(producto)
                dialog.dismiss()
            }
        }
        dialog.show()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.90).toInt()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Adapter interno ───────────────────────────────────────────────────
    class ProductosAdapter(private val onClick: (Producto) -> Unit) :
        androidx.recyclerview.widget.ListAdapter<Producto,
        ProductosAdapter.ViewHolder>(DIFF) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_producto, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(getItem(position))

        inner class ViewHolder(view: View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            private val tvNombre   = view.findViewById<android.widget.TextView>(R.id.tvNombre)
            private val tvPrecio   = view.findViewById<android.widget.TextView>(R.id.tvPrecioSugerido)
            private val tvStock    = view.findViewById<android.widget.TextView>(R.id.tvStock)
            private val ivAlerta   = view.findViewById<android.widget.ImageView>(R.id.ivStockAlerta)

            fun bind(p: Producto) {
                tvNombre.text = p.nombre
                tvPrecio.text = CurrencyFormatter.format(p.precioSugerido)
                tvStock.text  = "Stock: ${p.stockActual.toInt()}"
                ivAlerta.visibility = if (p.tieneStockBajo) View.VISIBLE else View.GONE
                itemView.setOnClickListener { onClick(p) }
            }
        }

        companion object {
            val DIFF = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Producto>() {
                override fun areItemsTheSame(a: Producto, b: Producto) = a.id == b.id
                override fun areContentsTheSame(a: Producto, b: Producto) = a == b
            }
        }
    }
}

// Constructor auxiliar para el preview del precio en el diálogo
private fun Producto(nombre: String, costoIng: Double, costoFijo: Double, margen: Double) =
    Producto(nombre = nombre, costoIngredientes = costoIng,
             costoFijoProrrateado = costoFijo, margenGanancia = margen)
