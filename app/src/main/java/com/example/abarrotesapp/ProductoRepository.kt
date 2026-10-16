
package com.example.abarrotesapp

import kotlinx.coroutines.flow.Flow

class ProductoRepository(
    private val productoDao: ProductoDao
) {

    // Obtener todos los productos
    fun obtenerTodos(): Flow<List<Producto>> {
        return productoDao.obtenerTodos()
    }

    // Buscar un producto por su ID
    suspend fun obtenerPorId(id: Int): Producto? {
        return productoDao.obtenerPorId(id)
    }

    // Registrar un producto
    suspend fun insertar(producto: Producto): Long {
        return productoDao.insertar(producto)
    }

    // Actualizar un producto
    suspend fun actualizar(producto: Producto) {
        productoDao.actualizar(producto)
    }

    // Eliminar un producto
    suspend fun eliminar(producto: Producto) {
        productoDao.eliminar(producto)
    }

    fun buscarPorNombre(texto: String): Flow<List<Producto>> {
        return productoDao.buscarPorNombre(texto)
    }

}
