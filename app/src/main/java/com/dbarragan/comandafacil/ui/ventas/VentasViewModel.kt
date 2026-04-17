package com.dbarragan.comandafacil.ui.ventas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dbarragan.comandafacil.data.model.DetalleVenta
import com.dbarragan.comandafacil.data.model.Producto
import com.dbarragan.comandafacil.data.repository.JornadaRepository
import com.dbarragan.comandafacil.data.repository.ProductosRepository
import com.dbarragan.comandafacil.data.repository.VentasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VentasViewModel(application: Application) : AndroidViewModel(application) {

    private val productosRepo= ProductosRepository(application)
    private val ventasRepo   = VentasRepository(application)
    private val jornadaRepo  = JornadaRepository(application)

    private val _productos = MutableLiveData<List<Producto>>()
    val productos: LiveData<List<Producto>> = _productos

    private val _ventasHoy = MutableLiveData<List<DetalleVenta>>()
    val ventasHoy: LiveData<List<DetalleVenta>> = _ventasHoy

    private val _ventaRegistrada = MutableLiveData<Boolean>()
    val ventaRegistrada: LiveData<Boolean> = _ventaRegistrada

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargar() {
        viewModelScope.launch(Dispatchers.IO) {
            _productos.postValue(productosRepo.listarActivos())
            val jornada = jornadaRepo.obtenerJornadaHoy()
            if (jornada != null) {
                _ventasHoy.postValue(ventasRepo.listarVentasDelDia(jornada.id))
            }
        }
    }

    fun registrarVenta(productoId: Int, cantidad: Double, precioVenta: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jornada = jornadaRepo.obtenerOCrearJornadaHoy()
                ventasRepo.registrar(jornada.id, productoId, cantidad, precioVenta)
                _ventasHoy.postValue(ventasRepo.listarVentasDelDia(jornada.id))
                _ventaRegistrada.postValue(true)
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }
}
