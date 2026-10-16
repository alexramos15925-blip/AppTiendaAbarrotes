package com.example.abarrotesapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductoViewModel(
    private val repositorio: ProductoRepository
) : ViewModel() {

    val productos: Flow<List<Producto>> =
        repositorio.obtenerTodos()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    fun registrarProducto(
        nombre: String,
        precioTexto: String,
        stockTexto: String
    ) {
        val nombreLimpio = nombre.trim()
        val precioNormalizado = precioTexto.trim().replace(',', '.')
        val precio = precioNormalizado.toBigDecimalOrNull()
        val stock = stockTexto.trim().toIntOrNull()

        if (nombreLimpio.isEmpty()) {
            _mensaje.value = "Escribe el nombre del producto."
            return
        }

        if (precio == null || precio <= java.math.BigDecimal.ZERO) {
            _mensaje.value = "El precio debe ser mayor que cero."
            return
        }

        if (stock == null || stock < 0) {
            _mensaje.value = "El stock debe ser un número entero igual o mayor que cero."
            return
        }

        val centimos = try {
            precio.movePointRight(2)
                .setScale(0, java.math.RoundingMode.UNNECESSARY)
                .longValueExact()
        } catch (e: ArithmeticException) {
            _mensaje.value = "El precio debe tener como máximo dos decimales."
            return
        }

        viewModelScope.launch {
            try {
                repositorio.insertar(
                    Producto(
                        nombre = nombreLimpio,
                        precioCentimos = centimos,
                        stock = stock
                    )
                )
                _mensaje.value = "Producto guardado correctamente."
            } catch (e: Exception) {
                _mensaje.value = "No se pudo guardar el producto."
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                repositorio.actualizar(producto)
                _mensaje.value = "Producto actualizado correctamente."
            } catch (e: Exception) {
                _mensaje.value = "No se pudo actualizar el producto."
            }
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                repositorio.eliminar(producto)
                _mensaje.value = "Producto eliminado correctamente."
            } catch (e: Exception) {
                _mensaje.value = "No se pudo eliminar el producto."
            }
        }
    }

    suspend fun buscarPorId(id: Int): Producto? {
        return repositorio.obtenerPorId(id)
    }

    fun buscarProductos(texto: String): Flow<List<Producto>> {
        return repositorio.buscarPorNombre(texto)
    }

}
