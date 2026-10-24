package `in`.ignishers.milkmanager2.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import `in`.ignishers.milkmanager2.R
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
        title = { Text(stringResource(R.string.add_route_group)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text(stringResource(R.string.route_name_label)) },
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
