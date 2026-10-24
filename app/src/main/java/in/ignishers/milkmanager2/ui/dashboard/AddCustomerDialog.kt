package `in`.ignishers.milkmanager2.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import `in`.ignishers.milkmanager2.R
import `in`.ignishers.milkmanager2.models.Customer
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDialog(
    initialCustomer: Customer? = null,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var mobile by remember { mutableStateOf(initialCustomer?.mobile ?: "") }
    var morningQtyStr by remember { mutableStateOf(initialCustomer?.defaultQtyMorning?.toPlainString()?.takeIf { it != "0.0" && it != "0" } ?: "") }
    var eveningQtyStr by remember { mutableStateOf(initialCustomer?.defaultQtyEvening?.toPlainString()?.takeIf { it != "0.0" && it != "0" } ?: "") }
    var openingDueStr by remember { mutableStateOf(initialCustomer?.currentDue?.toPlainString()?.takeIf { it != "0.0" && it != "0" } ?: "") }
    var entryMode by remember { mutableStateOf("Liters") }

    val currentMilkRate = BigDecimal("60.0")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCustomer == null) stringResource(R.string.add_customer) else stringResource(R.string.edit_customer)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text(stringResource(R.string.mobile_number)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.entry_mode), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = entryMode == "Liters",
                        onClick = { entryMode = "Liters" }
                    )
                    Text(stringResource(R.string.liters), modifier = Modifier.padding(end = 16.dp))

                    RadioButton(
                        selected = entryMode == "Rupees",
                        onClick = { entryMode = "Rupees" }
                    )
                    Text(stringResource(R.string.rupees))
                }

                val mLabel = if (entryMode == "Liters") stringResource(R.string.morning_liters) else stringResource(R.string.morning_rupees)
                OutlinedTextField(
                    value = morningQtyStr,
                    onValueChange = { morningQtyStr = it },
                    label = { Text(mLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                val eLabel = if (entryMode == "Liters") stringResource(R.string.evening_liters) else stringResource(R.string.evening_rupees)
                OutlinedTextField(
                    value = eveningQtyStr,
                    onValueChange = { eveningQtyStr = it },
                    label = { Text(eLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = openingDueStr,
                    onValueChange = { openingDueStr = it },
                    label = { Text(if (initialCustomer == null) stringResource(R.string.opening_due_rupees) else stringResource(R.string.current_due_rupees)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val morningVal = morningQtyStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val eveningVal = eveningQtyStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val openingDue = openingDueStr.toBigDecimalOrNull() ?: BigDecimal.ZERO

                    val morningLiters = if (entryMode == "Rupees") {
                        morningVal.divide(currentMilkRate, 2, RoundingMode.HALF_UP)
                    } else {
                        morningVal
                    }

                    val eveningLiters = if (entryMode == "Rupees") {
                        eveningVal.divide(currentMilkRate, 2, RoundingMode.HALF_UP)
                    } else {
                        eveningVal
                    }

                    val customer = initialCustomer ?: Customer()
                    customer.apply {
                        this.name = name
                        this.mobile = mobile
                        this.defaultQtyMorning = morningLiters
                        this.defaultQtyEvening = eveningLiters
                        this.currentDue = openingDue
                        if (initialCustomer == null) {
                            this.sortOrder = 0
                            this.syncSellerId = null
                        }
                    }
                    onSave(customer)
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
