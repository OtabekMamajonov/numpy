package com.dokonhisob.app.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dokonhisob.app.data.local.entity.Product
import com.dokonhisob.app.ui.common.formatMoney
import com.dokonhisob.app.ui.common.formatQuantity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(viewModel: ProductViewModel, currency: String) {
    val products by viewModel.products.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mahsulotlar") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingProduct = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Qo'shish")
            }
        }
    ) { padding ->
        if (products.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                Text("Hali mahsulot qo'shilmagan. Pastdagi + tugmasi orqali qo'shing.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.clickableColumn {
                                    editingProduct = product
                                    showDialog = true
                                }
                            ) {
                                Text(product.name, fontWeight = FontWeight.Bold)
                                Text("${formatQuantity(product.quantity)} ${product.unit} · sotish: ${formatMoney(product.sellPrice, currency)}")
                                Text("tan narx: ${formatMoney(product.purchasePrice, currency)} · ${product.category}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.delete(product) }) {
                                Icon(Icons.Default.Delete, contentDescription = "O'chirish")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEditProductDialog(
            initial = editingProduct,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.save(it)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditProductDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "Boshqa") }
    var purchasePrice by remember { mutableStateOf(initial?.purchasePrice?.toString() ?: "") }
    var sellPrice by remember { mutableStateOf(initial?.sellPrice?.toString() ?: "") }
    var quantity by remember { mutableStateOf(initial?.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(initial?.unit ?: "dona") }
    var minQuantity by remember { mutableStateOf(initial?.minQuantity?.toString() ?: "5") }

    val categories = listOf("Non mahsulotlari", "Ichimliklar", "Sut mahsulotlari", "Konservalar", "Shirinliklar", "Yuvish vositalari", "Boshqa")
    val units = listOf("dona", "kg", "litr", "quti")

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Yangi mahsulot" else "Mahsulotni tahrirlash") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nomi") })

                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategoriya") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach {
                            androidx.compose.material3.DropdownMenuItem(text = { Text(it) }, onClick = {
                                category = it
                                categoryExpanded = false
                            })
                        }
                    }
                }

                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("Tan narx") }
                )
                OutlinedTextField(
                    value = sellPrice,
                    onValueChange = { sellPrice = it },
                    label = { Text("Sotish narxi") }
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Miqdori") }
                )

                ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("O'lchov birligi") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        units.forEach {
                            androidx.compose.material3.DropdownMenuItem(text = { Text(it) }, onClick = {
                                unit = it
                                unitExpanded = false
                            })
                        }
                    }
                }

                OutlinedTextField(
                    value = minQuantity,
                    onValueChange = { minQuantity = it },
                    label = { Text("Kam qolish chegarasi") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val product = Product(
                    id = initial?.id ?: 0,
                    name = name.trim(),
                    category = category,
                    purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                    sellPrice = sellPrice.toDoubleOrNull() ?: 0.0,
                    quantity = quantity.toDoubleOrNull() ?: 0.0,
                    unit = unit,
                    minQuantity = minQuantity.toDoubleOrNull() ?: 0.0,
                    createdAt = initial?.createdAt ?: System.currentTimeMillis()
                )
                if (product.name.isNotBlank()) onSave(product)
            }) {
                Text("Saqlash")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish") }
        }
    )
}

private fun Modifier.clickableColumn(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
