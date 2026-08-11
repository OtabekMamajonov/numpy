package com.dokonhisob.app.ui.debts

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.dokonhisob.app.ui.common.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    viewModel: DebtListViewModel,
    currency: String,
    onOpenDetail: (Long) -> Unit
) {
    val debts by viewModel.debts.collectAsState()
    var showAddCustomer by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Qarz daftar") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCustomer = true }) {
                Icon(Icons.Default.Add, contentDescription = "Mijoz qo'shish")
            }
        }
    ) { padding ->
        val withDebt = debts.filter { it.remaining > 0 }
        val withoutDebt = debts.filter { it.remaining <= 0 }

        if (debts.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(padding).padding(24.dp)) {
                Text("Hozircha mijozlar yo'q. + tugmasi orqali qo'shing.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (withDebt.isNotEmpty()) {
                    item {
                        Text("Qarzdorlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(withDebt, key = { it.customer.id }) { debt ->
                        DebtCard(name = debt.customer.name, remaining = debt.remaining, currency = currency) {
                            onOpenDetail(debt.customer.id)
                        }
                    }
                }
                if (withoutDebt.isNotEmpty()) {
                    item {
                        Text("Boshqa mijozlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(withoutDebt, key = { it.customer.id }) { debt ->
                        DebtCard(name = debt.customer.name, remaining = debt.remaining, currency = currency) {
                            onOpenDetail(debt.customer.id)
                        }
                    }
                }
            }
        }
    }

    if (showAddCustomer) {
        AlertDialog(
            onDismissRequest = { showAddCustomer = false },
            title = { Text("Yangi mijoz") },
            text = {
                var name by remember { mutableStateOf("") }
                var phone by remember { mutableStateOf("") }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ismi") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefon (ixtiyoriy)") })
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addCustomer(name.trim(), phone.ifBlank { null })
                            showAddCustomer = false
                        }
                    }) {
                        Text("Saqlash")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddCustomer = false }) { Text("Yopish") }
            }
        )
    }
}

@Composable
private fun DebtCard(name: String, remaining: Double, currency: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, fontWeight = FontWeight.Bold)
            Text(
                if (remaining > 0) formatMoney(remaining, currency) else "Qarzi yo'q",
                color = if (remaining > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDetailScreen(viewModel: DebtDetailViewModel, currency: String, onBack: () -> Unit) {
    val debt by viewModel.debt.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(debt?.customer?.name ?: "Mijoz") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
            val current = debt
            if (current != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Jami nasiya: ${formatMoney(current.totalDebt, currency)}")
                        Text("To'langan: ${formatMoney(current.totalPaid, currency)}")
                        Text(
                            "Qoldiq: ${formatMoney(current.remaining, currency)}",
                            fontWeight = FontWeight.Bold,
                            color = if (current.remaining > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Button(
                    onClick = { showPaymentDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("To'lov qo'shish")
                }
            }
        }
    }

    if (showPaymentDialog) {
        var amount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("To'lov qo'shish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Summa") })
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Izoh (ixtiyoriy)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = amount.toDoubleOrNull() ?: 0.0
                    if (value > 0) {
                        viewModel.addPayment(value, note.ifBlank { null })
                        showPaymentDialog = false
                    }
                }) {
                    Text("Saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) { Text("Bekor qilish") }
            }
        )
    }
}
