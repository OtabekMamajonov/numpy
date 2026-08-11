package com.dokonhisob.app.ui.common

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("uz")).apply {
    maximumFractionDigits = 0
}

private val decimalFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("uz")).apply {
    maximumFractionDigits = 2
}

fun formatMoney(amount: Double, currency: String = "so'm"): String =
    "${numberFormat.format(amount)} $currency"

fun formatQuantity(quantity: Double): String = decimalFormat.format(quantity)

private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))
fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
