package com.example.abarrotesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val baseDatos = AppDatabase.obtenerBaseDatos(this)
        val repositorio = ProductoRepository(baseDatos.productoDao())
        val fabrica = ProductoViewModelFactory(repositorio)

        setContent {
            val productoViewModel: ProductoViewModel = viewModel(
                factory = fabrica
            )

            MaterialTheme {
                PantallaProductos(productoViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProductos(viewModel: ProductoViewModel) {

    val productos by viewModel.productos.collectAsState(
        initial = emptyList()
    )
    val mensaje by viewModel.mensaje.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var busqueda by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf(emptyList<Producto>()) }
    var mostrarSugerencias by remember { mutableStateOf(false) }

    var productoEditando by remember { mutableStateOf<Producto?>(null) }
    var productoPorEliminar by remember { mutableStateOf<Producto?>(null) }

    LaunchedEffect(busqueda) {
        if (busqueda.isBlank()) {
            sugerencias = emptyList()
            mostrarSugerencias = false
        } else {
            delay(250)
            viewModel.buscarProductos(busqueda.trim())
                .collectLatest { resultados ->
                    sugerencias = resultados
                    mostrarSugerencias = true
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tienda de Abarrotes") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Buscar producto",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar producto por nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (mostrarSugerencias && sugerencias.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sugerencias.forEach { producto ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    busqueda = producto.nombre
                                    mostrarSugerencias = false
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "S/ ${
                                        String.format(
                                            Locale.US,
                                            "%.2f",
                                            producto.precioCentimos / 100.0
                                        )
                                    } · Stock: ${producto.stock}"
                                )
                            }
                        }
                    }
                }
            }


            Text(
                text = "Registrar producto",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio de venta (S/)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Stock (unidades)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.registrarProducto(nombre, precio, stock)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar producto")
            }

            if (mensaje != null) {
                Text(
                    text = mensaje!!,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Productos registrados (${productos.size})",
                style = MaterialTheme.typography.titleLarge
            )

            if (productos.isEmpty()) {
                Text("Todavía no hay productos registrados.")
            } else {
                productos.forEach { producto ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = producto.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Precio: S/ ${
                                    String.format(
                                        Locale.US,
                                        "%.2f",
                                        producto.precioCentimos / 100.0
                                    )
                                }"
                            )

                            Text("Stock: ${producto.stock} unidades")
                            Text("Código: ${producto.id}")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        productoEditando = producto
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Editar")
                                }

                                OutlinedButton(
                                    onClick = {
                                        productoPorEliminar = producto
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para editar
    productoEditando?.let { producto ->

        var nombreEditado by remember(producto.id) {
            mutableStateOf(producto.nombre)
        }

        var precioEditado by remember(producto.id) {
            mutableStateOf(
                BigDecimal.valueOf(producto.precioCentimos, 2)
                    .toPlainString()
            )
        }

        var stockEditado by remember(producto.id) {
            mutableStateOf(producto.stock.toString())
        }

        AlertDialog(
            onDismissRequest = { productoEditando = null },
            title = { Text("Editar producto") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nombreEditado,
                        onValueChange = { nombreEditado = it },
                        label = { Text("Nombre") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = precioEditado,
                        onValueChange = { precioEditado = it },
                        label = { Text("Precio (S/)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = stockEditado,
                        onValueChange = { stockEditado = it },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nombreLimpio = nombreEditado.trim()
                        val precioNormalizado =
                            precioEditado.trim().replace(',', '.')
                        val precioDecimal =
                            precioNormalizado.toBigDecimalOrNull()
                        val stockNumero = stockEditado.trim().toIntOrNull()

                        val centimos = try {
                            precioDecimal
                                ?.movePointRight(2)
                                ?.setScale(0, RoundingMode.UNNECESSARY)
                                ?.longValueExact()
                        } catch (e: ArithmeticException) {
                            null
                        }

                        if (
                            nombreLimpio.isNotEmpty() &&
                            precioDecimal != null &&
                            precioDecimal > BigDecimal.ZERO &&
                            centimos != null &&
                            stockNumero != null &&
                            stockNumero >= 0
                        ) {
                            viewModel.actualizarProducto(
                                producto.copy(
                                    nombre = nombreLimpio,
                                    precioCentimos = centimos,
                                    stock = stockNumero
                                )
                            )
                            productoEditando = null
                        }
                    }
                ) {
                    Text("Guardar cambios")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productoEditando = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirmación antes de eliminar
    productoPorEliminar?.let { producto ->

        AlertDialog(
            onDismissRequest = { productoPorEliminar = null },
            title = { Text("Eliminar producto") },
            text = {
                Text("¿Seguro que deseas eliminar ${producto.nombre}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarProducto(producto)
                        productoPorEliminar = null
                    }
                ) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productoPorEliminar = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
