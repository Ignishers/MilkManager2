package `in`.ignishers.milkmanager2.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import `in`.ignishers.milkmanager2.models.RouteGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRouteDialog(
    parentId: Long?,
    onDismiss: () -> Unit,
    onSave: (RouteGroup) -> Unit
) {
    var routeName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Route Group") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Route Name (e.g. Area, Colony, Street)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val routeGroup = RouteGroup().apply {
                        this.name = routeName
                        this.parentId = parentId
                        this.sortOrder = 0
                        this.syncSellerId = null
                    }
                    onSave(routeGroup)
                },
                enabled = routeName.isNotBlank()
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
