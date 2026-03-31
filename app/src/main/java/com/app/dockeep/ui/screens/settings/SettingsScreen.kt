package com.app.dockeep.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.ViewCompact
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.dockeep.ui.MainViewModel
import com.app.dockeep.ui.components.ThemeSelectionDialog
import com.app.dockeep.utils.Helper.getAppVersion
import com.app.dockeep.utils.ThemeMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onGoBack: () -> Unit,
) {
    val mainVM: MainViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
    val theme by mainVM.theme.collectAsState()
    var deleteOrig by mainVM.deleteOriginal
    var compactView by mainVM.compactView
    var showHiddenFiles by mainVM.showHiddenFiles

    val contentPathLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            mainVM.setContentPathUri(result)
        }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings", fontWeight = FontWeight.SemiBold) }, navigationIcon = {
                IconButton(onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null
                    )
                }
            })
        }) {
        BackHandler {
            onGoBack()
        }

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val context = LocalContext.current
            var openThemeDialog by remember { mutableStateOf(false) }
            val themeOptions = listOf(ThemeMode.AUTO, ThemeMode.DARK, ThemeMode.LIGHT)
            val uriHandler = LocalUriHandler.current

            SettingsGroup(title = "General") {
                SettingsItem(
                    title = "Save location",
                    subtitle = "Choose where imported files will be stored",
                    icon = Icons.Outlined.Folder,
                    onClick = {
                        contentPathLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
                    }
                )
                SettingsItem(
                    title = "Delete originals",
                    subtitle = "Remove source files after they are imported",
                    icon = Icons.Outlined.Delete,
                    trailingContent = {
                        Switch(
                            checked = deleteOrig,
                            onCheckedChange = { checked -> mainVM.setDeleteOriginal(checked) })
                    }
                )
            }

            SettingsGroup(title = "Appearance & Layout") {
                SettingsItem(
                    title = "App theme",
                    subtitle = "Switch between light, dark, or system default",
                    icon = Icons.Outlined.ColorLens,
                    onClick = { openThemeDialog = true }
                )
                SettingsItem(
                    title = "Compact view",
                    subtitle = "Show more items on screen",
                    icon = Icons.Outlined.ViewCompact,
                    trailingContent = {
                        Switch(
                            checked = compactView,
                            onCheckedChange = { checked -> mainVM.setCompactView(checked) })
                    }
                )
                SettingsItem(
                    title = "Show hidden files",
                    subtitle = "Files starting with a dot",
                    icon = Icons.Outlined.Visibility,
                    trailingContent = {
                        Switch(
                            checked = showHiddenFiles,
                            onCheckedChange = { checked -> mainVM.setShowHiddenFiles(checked) })
                    }
                )
            }

            SettingsGroup(title = "About & Feedback") {
                SettingsItem(
                    title = "Share app",
                    subtitle = "Let others know about Dockeep",
                    icon = Icons.Outlined.Share,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "https://github.com/mattgdot/Dockeep")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        try {
                            context.startActivity(shareIntent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Can't open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                SettingsItem(
                    title = "Contact developer",
                    subtitle = "Send feedback or report issues",
                    icon = Icons.Outlined.Email,
                    onClick = {
                        val emailIntent = Intent(
                            Intent.ACTION_SENDTO, "mailto:decosoftapps@gmail.com".toUri()
                        )
                        try {
                            context.startActivity(emailIntent)
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, "Can't open", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            SettingsGroup(title = "Legal") {
                SettingsItem(
                    title = "Terms & Conditions",
                    subtitle = "Read the legal terms for using Dockeep",
                    icon = Icons.Outlined.VerifiedUser,
                    onClick = {
                        try {
                            uriHandler.openUri("https://github.com/mattgdot/Dockeep/blob/main/terms_conditions.md")
                        } catch (_: Exception) {
                            Toast.makeText(context, "Can't open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "Learn how your data is handled",
                    icon = Icons.Outlined.Lock,
                    onClick = {
                        try {
                            uriHandler.openUri("https://github.com/mattgdot/Dockeep/blob/main/privacy_policy.md")
                        } catch (_: Exception) {
                            Toast.makeText(context, "Can't open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Version: ${context.getAppVersion()?.versionName ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            if (openThemeDialog) {
                ThemeSelectionDialog(
                    onDismiss = { openThemeDialog = false },
                    onSubmit = { theme -> mainVM.setAppTheme(theme) },
                    themeOptions = themeOptions,
                    initialTheme = theme
                )
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}
