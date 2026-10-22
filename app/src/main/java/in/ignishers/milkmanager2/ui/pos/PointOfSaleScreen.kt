package `in`.ignishers.milkmanager2.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointOfSaleScreen(
    customerId: Long,
    onBack: () -> Unit,
    viewModel: PointOfSaleViewModel = koinViewModel()
) {
    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    val customer by viewModel.customer.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val session by viewModel.session.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val amount by viewModel.amount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "Loading...", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.saveEntry(onSuccess = onBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SAVE ENTRY (₹${amount.toPlainString()})", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Date:", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(currentDate, style = MaterialTheme.typography.titleMedium)
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

            // Quantity Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Quantity (Liters)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        FilledIconButton(onClick = { viewModel.decrementQuantity() }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Text("-", style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            text = quantity.toPlainString(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FilledIconButton(onClick = { viewModel.incrementQuantity() }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}
