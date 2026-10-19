package `in`.ignishers.milkmanager2.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import `in`.ignishers.milkmanager2.models.MilkRate
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateMilkPriceDialog(
    currentPrice: BigDecimal?,
    onDismiss: () -> Unit,
    onSave: (MilkRate) -> Unit
) {
    var priceStr by remember { mutableStateOf(currentPrice?.toPlainString() ?: "") }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val selectedDateStr = datePickerState.selectedDateMillis?.let { formatter.format(Date(it)) } ?: "Select Date"

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Milk Price") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price per Liter (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Effective Date: $selectedDateStr")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceStr.toBigDecimalOrNull()
                    val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    if (price != null && price > BigDecimal.ZERO) {
                        val rate = MilkRate(
                            price = price,
                            effectiveDate = dateMillis.toString()
                        )
                        onSave(rate)
                    }
                },
                enabled = priceStr.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
