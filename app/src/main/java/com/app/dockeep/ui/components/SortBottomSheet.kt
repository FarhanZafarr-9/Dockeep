package com.app.dockeep.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    initial: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        onDismissRequest = onDismiss,
    ) {
        val radioOptions = listOf("Name", "Size", "Date")

        var selectedBase by remember {
            mutableStateOf(initial.split(" ").first().ifBlank { "Name" })
        }
        var isAscending by remember {
            mutableStateOf(initial.contains("Ascending"))
        }

        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .selectableGroup()
        ) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            radioOptions.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (text == selectedBase),
                            onClick = {
                                selectedBase = text
                                val order = if (isAscending) "Ascending" else "Descending"
                                onSelect("$selectedBase $order")
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == selectedBase),
                        onClick = null
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.size(16.dp))
                    Text(
                        text = if (isAscending) "Ascending" else "Descending",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Switch(
                    checked = isAscending,
                    onCheckedChange = {
                        isAscending = it
                        val order = if (isAscending) "Ascending" else "Descending"
                        onSelect("$selectedBase $order")
                    }
                )
            }
        }
    }
}