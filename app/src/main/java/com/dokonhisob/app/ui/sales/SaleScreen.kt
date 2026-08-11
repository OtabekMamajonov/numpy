package com.dokonhisob.app.ui.sales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.dokonhisob.app.data.local.entity.Customer
import com.dokonhisob.app.ui.common.formatMoney
import com.dokonhisob.app.ui.common.formatQuantity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleScreen(viewModel: SaleViewModel, currency: String) {
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val state by viewModel.uiState.collectAsState()

    var query by remember { mutableStateOf("") }
    var showCheckout by remember { mutableStateOf(false) }

    val filtered = remember(products, query) {
        if (query.isBlank()) products else products.filter { it.name.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sotuv (Kassa)") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Mahsulot qidirish") },
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.addToCart(product) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(product.name, fontWeight = FontWeight.Bold)
                                Text("${formatQuantity(product.quantity)} ${product.unit} mavjud", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatMoney(product.sellPrice, currency))
                        }
                    }
                }
            }

            if (state.cart.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Savat", fontWeight = FontWeight.Bold)
                        state.cart.forEach { line ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(line.product.name, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.updateQuantity(line.product, line.quantity - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Kamaytirish")
                                }
                                Text(formatQuantity(line.quantity))
                                IconButton(onClick = { viewModel.updateQuantity(line.product, line.quantity + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Ko'paytirish")
                                }
                                Text(formatMoney(line.product.sellPrice * line.quantity, currency))
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Jami:", fontWeight = FontWeight.Bold)
                            Text(formatMoney(state.total, currency), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showCheckout = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Sotuvni yakunlash")
                        }
                    }
                }
            }
        }
    }

    if (showCheckout) {
        CheckoutDialog(
            total = state.total,
            currency = currency,
            customers = customers,
            onDismiss = { showCheckout = false },
            onConfirm = { customer, isDebt, note ->
                viewModel.checkout(customer, isDebt, note) {
                    showCheckout = false
                }
            },
            onCreateCustomer = { name, phone, callback -> viewModel.saveNewCustomer(name, phone, callback) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutDialog(
    total: Double,
    currency: String,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (Customer?, Boolean, String?) -> Unit,
    onCreateCustomer: (String, String?, (Customer) -> Unit) -> Unit
) {
    var isDebt by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var newCustomerName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var customerMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sotuvni tasdiqlash") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Jami summa: ${formatMoney(total, currency)}", fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isDebt = !isDebt },
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Checkbox(checked = isDebt, onCheckedChange = { isDebt = it })
                    Text("Nasiyaga (qarzga) sotish")
                }

                if (isDebt) {
                    ExposedDropdownMenuBox(expanded = customerMenuExpanded, onExpandedChange = { customerMenuExpanded = it }) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mijozni tanlang") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = customerMenuExpanded, onDismissRequest = { customerMenuExpanded = false }) {
                            customers.forEach { customer ->
                                DropdownMenuItem(text = { Text(customer.name) }, onClick = {
                                    selectedCustomer = customer
                                    customerMenuExpanded = false
                                })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newCustomerName,
                        onValueChange = { newCustomerName = it },
                        label = { Text("Yoki yangi mijoz ismi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Izoh (ixtiyoriy)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (isDebt && selectedCustomer == null && newCustomerName.isNotBlank()) {
                    onCreateCustomer(newCustomerName.trim(), null) { created ->
                        onConfirm(created, true, note.ifBlank { null })
                    }
                } else {
                    onConfirm(if (isDebt) selectedCustomer else null, isDebt, note.ifBlank { null })
                }
            }) {
                Text("Tasdiqlash")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish") }
        }
    )
}
