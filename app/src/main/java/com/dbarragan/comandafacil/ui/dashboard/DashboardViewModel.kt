package com.dbarragan.comandafacil.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dbarragan.comandafacil.data.model.Jornada
import com.dbarragan.comandafacil.data.model.Producto
import com.dbarragan.comandafacil.data.repository.JornadaRepository
import com.dbarragan.comandafacil.data.repository.ProductosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val jornadaRepo  = JornadaRepository(application)
    private val productosRepo= ProductosRepository(application)

    private val _jornada = MutableLiveData<Jornada?>()
    val jornada: LiveData<Jornada?> = _jornada

    private val _productosConStockBajo = MutableLiveData<List<Producto>>()
    val productosConStockBajo: LiveData<List<Producto>> = _productosConStockBajo

    private val _gastoRegistrado = MutableLiveData<Boolean>()
    val gastoRegistrado: LiveData<Boolean> = _gastoRegistrado

    fun cargar() {
        viewModelScope.launch(Dispatchers.IO) {
            _jornada.postValue(jornadaRepo.obtenerOCrearJornadaHoy())
            _productosConStockBajo.postValue(productosRepo.listarConStockBajo())
        }
    }

    fun registrarGasto(descripcion: String, monto: Double, tipo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val jornada = jornadaRepo.obtenerJornadaHoy() ?: return@launch
            val gasto = com.dbarragan.comandafacil.data.model.GastoOperativo(
                jornadaId   = jornada.id,
                descripcion = descripcion,
                monto       = monto,
                tipo        = tipo,
                fecha       = com.dbarragan.comandafacil.util.DateHelper.ahora()
            )
            jornadaRepo.registrarGasto(gasto)
            _jornada.postValue(jornadaRepo.obtenerJornadaHoy())
            _gastoRegistrado.postValue(true)
        }
    }
}
