
package com.example.abarrotesapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    // Consultar todos los productos
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Producto>>

    // Buscar un producto por su ID
    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Producto?

    // Registrar un producto
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(producto: Producto): Long

    // Actualizar un producto existente
    @Update
    suspend fun actualizar(producto: Producto)

    // Eliminar un producto
    @Delete
    suspend fun eliminar(producto: Producto)

    @Query("""
    SELECT * FROM productos
    WHERE nombre LIKE '%' || :texto || '%'
    ORDER BY nombre ASC
""")
    fun buscarPorNombre(texto: String): Flow<List<Producto>>

}
