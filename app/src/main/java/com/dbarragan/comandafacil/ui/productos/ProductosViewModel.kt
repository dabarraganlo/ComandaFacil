package com.dbarragan.comandafacil.ui.productos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dbarragan.comandafacil.data.model.Producto
import com.dbarragan.comandafacil.data.repository.ProductosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductosViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ProductosRepository(application)

    private val _productos = MutableLiveData<List<Producto>>()
    val productos: LiveData<List<Producto>> = _productos

    private val _guardado = MutableLiveData<Boolean>()
    val guardado: LiveData<Boolean> = _guardado

    fun cargar() {
        viewModelScope.launch(Dispatchers.IO) {
            _productos.postValue(repo.listarActivos())
        }
    }

    fun guardar(p: Producto) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.guardar(p)
            _productos.postValue(repo.listarActivos())
            _guardado.postValue(true)
        }
    }
}
