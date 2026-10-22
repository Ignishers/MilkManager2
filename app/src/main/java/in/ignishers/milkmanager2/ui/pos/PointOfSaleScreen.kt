package `in`.ignishers.milkmanager2.ui.pos

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.util.Calendar
import java.util.Locale
import `in`.ignishers.milkmanager2.models.DailyTransaction
import `in`.ignishers.milkmanager2.models.Payment

sealed class TodayHistoryItem {
    data class Milk(val transaction: DailyTransaction) : TodayHistoryItem()
    data class Pay(val payment: Payment) : TodayHistoryItem()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PointOfSaleScreen(
    customerId: Long,
    onBack: () -> Unit,
    viewModel: PointOfSaleViewModel = koinViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    val customer by viewModel.customer.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val session by viewModel.session.collectAsState()
    val milkRate by viewModel.milkRate.collectAsState()
    val todayTransactions by viewModel.todayTransactions.collectAsState()
    val todayPayments by viewModel.todayPayments.collectAsState()

    var showQuickMenu by remember { mutableStateOf(false) }
    var showManualEntrySheet by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<TodayHistoryItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(customer?.name ?: "Loading...", fontWeight = FontWeight.Bold)
                        if (customer?.currentDue != null && customer?.currentDue!! > BigDecimal.ZERO) {
                            Text(
                                text = "Due: ₹${customer?.currentDue}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.errorContainer
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showPaymentDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text("+ Pay", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Auto",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = customer?.autoEntryEnabled == true,
                            onCheckedChange = { enabled ->
                                viewModel.toggleCustomerAutoEntry(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                                checkedBorderColor = MaterialTheme.colorScheme.onPrimary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                uncheckedTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                uncheckedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Date & Due Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Rate", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${milkRate}/L", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Due", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "₹${customer?.currentDue ?: BigDecimal.ZERO}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if ((customer?.currentDue ?: BigDecimal.ZERO) > BigDecimal.ZERO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Session Segmented Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SessionButton(
                    text = "Morning",
                    isSelected = session == "Morning",
                    onClick = { viewModel.setSession("Morning") },
                    modifier = Modifier.weight(1f)
                )
                SessionButton(
                    text = "Evening",
                    isSelected = session == "Evening",
                    onClick = { viewModel.setSession("Evening") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Entry & Manual Entry Card (Replaces old Quantity Card)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Add Milk Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Row with Quick Extras Dropdown and Manual Entry Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dropdown Menu Button for Quick Quantities
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showQuickMenu = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("Quick Qty", fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = showQuickMenu,
                                onDismissRequest = { showQuickMenu = false }
                            ) {
                                val quickOptions = listOf(
                                    "10rs" to "₹10",
                                    "20rs" to "₹20",
                                    "30rs" to "₹30",
                                    "50rs" to "₹50",
                                    "250ml" to "250 ml",
                                    "500ml" to "500 ml",
                                    "750ml" to "750 ml",
                                    "1l" to "1 Litre"
                                )
                                quickOptions.forEach { (key, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            showQuickMenu = false
                                            viewModel.addQuickEntry(key)
                                            Toast.makeText(context, "Added $label entry", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }

                        // Manual Entry Button
                        Button(
                            onClick = { showManualEntrySheet = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manual Entry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Today's Entry History Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (todayTransactions.isNotEmpty() || todayPayments.isNotEmpty()) {
                            Text(
                                text = "Hold entry to delete",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (todayTransactions.isEmpty() && todayPayments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No entries recorded for today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Milk transactions
                            todayTransactions.forEach { trans ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {},
                                            onLongClick = {
                                                itemToDelete = TodayHistoryItem.Milk(trans)
                                            }
                                        ),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (trans.session == "Morning") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        text = trans.session ?: "Milk",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (trans.session == "Morning") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${trans.quantity ?: 0} Litres",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (!trans.timestamp.isNullOrBlank()) {
                                                Text(
                                                    text = trans.timestamp,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = "+ ₹${trans.amount ?: 0}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            // Payments
                            todayPayments.forEach { pay ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {},
                                            onLongClick = {
                                                itemToDelete = TodayHistoryItem.Pay(pay)
                                            }
                                        ),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Payment (${pay.paymentMode ?: "Cash"})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                            Text(
                                                text = "Received",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "- ₹${pay.amount}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Entry Bottom Sheet (Slided-up Animated Dialog)
    if (showManualEntrySheet) {
        ManualEntryBottomSheet(
            initialDate = currentDate,
            initialSession = session,
            milkRate = milkRate,
            onDismiss = { showManualEntrySheet = false },
            onSave = { date, entrySession, entryType, value ->
                viewModel.addManualEntry(date, entrySession, entryType, value)
                Toast.makeText(context, "Manual entry saved!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Record Payment Dialog
    if (showPaymentDialog) {
        RecordPaymentDialog(
            initialDate = currentDate,
            onDismiss = { showPaymentDialog = false },
            onSave = { amount, mode, date ->
                viewModel.addPayment(amount, mode, date)
                Toast.makeText(context, "Payment of ₹$amount recorded!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Entry?", fontWeight = FontWeight.Bold) },
            text = {
                val message = when (val item = itemToDelete) {
                    is TodayHistoryItem.Milk -> "Are you sure you want to delete this milk transaction of ${item.transaction.quantity}L (₹${item.transaction.amount})? Customer due balance will be reduced by ₹${item.transaction.amount}."
                    is TodayHistoryItem.Pay -> "Are you sure you want to delete this payment of ₹${item.payment.amount}? Customer due balance will be restored by ₹${item.payment.amount}."
                    null -> ""
                }
                Text(message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (val item = itemToDelete) {
                            is TodayHistoryItem.Milk -> {
                                viewModel.deleteMilkTransaction(item.transaction)
                                Toast.makeText(context, "Milk entry deleted", Toast.LENGTH_SHORT).show()
                            }
                            is TodayHistoryItem.Pay -> {
                                viewModel.deletePayment(item.payment)
                                Toast.makeText(context, "Payment entry deleted", Toast.LENGTH_SHORT).show()
                            }
                            null -> {}
                        }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryBottomSheet(
    initialDate: String,
    initialSession: String,
    milkRate: BigDecimal,
    onDismiss: () -> Unit,
    onSave: (date: String, session: String, entryType: String, value: BigDecimal) -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedSession by remember { mutableStateOf(initialSession) }
    var selectedEntryType by remember { mutableStateOf("Litres") } // "Litres", "ml", "Rupee"
    var inputValue by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Manual Milk Entry",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Date Change
            OutlinedCard(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Date: $selectedDate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Change Date", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            // Session Selection
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SessionButton(
                    text = "Morning",
                    isSelected = selectedSession == "Morning",
                    onClick = { selectedSession = "Morning" },
                    modifier = Modifier.weight(1f)
                )
                SessionButton(
                    text = "Evening",
                    isSelected = selectedSession == "Evening",
                    onClick = { selectedSession = "Evening" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Entry Type Selection (Only one can be selected)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Entry Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedEntryType == "Litres",
                        onClick = { selectedEntryType = "Litres" },
                        label = { Text("Litres") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedEntryType == "ml",
                        onClick = { selectedEntryType = "ml" },
                        label = { Text("ml") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedEntryType == "Rupee",
                        onClick = { selectedEntryType = "Rupee" },
                        label = { Text("Rupee (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quantity Input Box
            val labelText = when (selectedEntryType) {
                "Litres" -> "Quantity (in Litres)"
                "ml" -> "Quantity (in Millilitres)"
                else -> "Amount (in Rupees)"
            }
            val suffixText = when (selectedEntryType) {
                "Litres" -> "L"
                "ml" -> "ml"
                else -> "₹"
            }

            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text(labelText) },
                suffix = { Text(suffixText, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Live Calculation Preview
            val numValue = inputValue.toDoubleOrNull()
            if (numValue != null && numValue > 0) {
                val previewText = when (selectedEntryType) {
                    "Litres" -> {
                        val amt = numValue * milkRate.toDouble()
                        "Qty: $numValue L • Rate: ₹$milkRate/L • Total: ₹%.2f".format(amt)
                    }
                    "ml" -> {
                        val ltr = numValue / 1000.0
                        val amt = ltr * milkRate.toDouble()
                        "Qty: %.3f L ($numValue ml) • Total: ₹%.2f".format(ltr, amt)
                    }
                    else -> {
                        val ltr = if (milkRate.toDouble() > 0) numValue / milkRate.toDouble() else 0.0
                        "Amount: ₹$numValue • Qty: %.2f L (at ₹$milkRate/L)".format(ltr)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = previewText,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Save Entry Button
            Button(
                onClick = {
                    val dec = inputValue.toBigDecimalOrNull()
                    if (dec != null && dec > BigDecimal.ZERO) {
                        onSave(selectedDate, selectedSession, selectedEntryType, dec)
                        onDismiss()
                    }
                },
                enabled = numValue != null && numValue > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAVE ENTRY", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onSave: (amount: BigDecimal, mode: String, date: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = paymentMode == "Cash",
                        onClick = { paymentMode = "Cash" },
                        label = { Text("Cash") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = paymentMode == "UPI",
                        onClick = { paymentMode = "UPI" },
                        label = { Text("UPI") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toBigDecimalOrNull()
                    if (amt != null && amt > BigDecimal.ZERO) {
                        onSave(amt, paymentMode, initialDate)
                        onDismiss()
                    }
                },
                enabled = amountText.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SessionButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}
