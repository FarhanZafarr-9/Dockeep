package com.app.dockeep.ui.screens.files.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.utils.Helper.getTimeAgo
import com.app.dockeep.utils.Helper.humanReadableSize

@Composable
fun FileListItem(
    compact: Boolean = false,
    item: DocumentItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    isSelected: Boolean,
    selectionMode: Boolean,
    addToSelected: (DocumentItem) -> Unit,
    removeFromSelected: (DocumentItem) -> Unit,
    onLongClick: () -> Unit
) {
    ListItem(
        colors = if(isSelected)  ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) else ListItemDefaults.colors(),
        headlineContent = {
            Text(
                item.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = (if (compact) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium).copy(
                    fontWeight = if (item.isFolder) FontWeight.Bold else FontWeight.Medium,
                    lineHeight = if (compact) 20.sp else 24.sp
                )
            )
        },
        supportingContent = {
            Text(
                if (item.isFolder) "Folder" else "${humanReadableSize(item.size!!)} • ${getTimeAgo(item.date!!)}",
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = (if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium).copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = if (compact) 16.sp else 20.sp
                ),
                modifier = Modifier.padding(top = if (compact) 0.dp else 2.dp)
            )
        },
        leadingContent = {
                Box(
                    modifier = Modifier
                        .size(if (compact) 40.dp else 50.dp)
                        .clip(RoundedCornerShape(if (compact) 10.dp else 14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if(item.isFolder) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null
                        )
                    } else {
                        var ext = item.mimeType.substringAfter("/")
                        if(ext.length > 5 || ext.isEmpty()) ext = item.name.substringAfterLast(".")
                        Text(
                            text = ext,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

        },
        trailingContent = {
            if (selectionMode) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                }
            } else {
                IconButton(onClick = onMoreClick) { Icon(Icons.Default.MoreVert, null) }
            }
        },
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(2.dp)
            .combinedClickable(
                onClick = {
                    if (selectionMode) {
                        if (isSelected) {
                            removeFromSelected(item)
                        } else {
                            addToSelected(item)
                        }
                    } else {
                        onClick()
                    }
                },
                onLongClick = {
                    if (selectionMode) {
                        if (isSelected) {
                            removeFromSelected(item)
                        } else {
                            addToSelected(item)
                        }
                    } else {
                        onLongClick()
                    }
                },
            ),
    )
}