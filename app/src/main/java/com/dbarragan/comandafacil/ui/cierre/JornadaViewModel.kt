package com.dbarragan.comandafacil.ui.cierre

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dbarragan.comandafacil.data.model.Jornada
import com.dbarragan.comandafacil.data.repository.JornadaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JornadaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = JornadaRepository(application)

    private val _jornadaHoy = MutableLiveData<Jornada?>()
    val jornadaHoy: LiveData<Jornada?> = _jornadaHoy

    private val _historial = MutableLiveData<List<Jornada>>()
    val historial: LiveData<List<Jornada>> = _historial

    private val _jornadaCerrada = MutableLiveData<Jornada?>()
    val jornadaCerrada: LiveData<Jornada?> = _jornadaCerrada

    fun cargar() {
        viewModelScope.launch(Dispatchers.IO) {
            _jornadaHoy.postValue(repo.obtenerOCrearJornadaHoy())
            _historial.postValue(repo.listarHistorial())
        }
    }

    fun cerrarJornada() {
        viewModelScope.launch(Dispatchers.IO) {
            val jornada = repo.obtenerJornadaHoy() ?: return@launch
            if (jornada.estaCerrada) return@launch
            val cerrada = repo.cerrarJornada(jornada.id)
            _jornadaCerrada.postValue(cerrada)
            _jornadaHoy.postValue(cerrada)
            _historial.postValue(repo.listarHistorial())
        }
    }
}
