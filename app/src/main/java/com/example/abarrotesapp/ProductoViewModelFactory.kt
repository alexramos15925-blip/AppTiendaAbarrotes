
package com.example.abarrotesapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProductoViewModelFactory(
    private val repositorio: ProductoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ProductoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductoViewModel(repositorio) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
