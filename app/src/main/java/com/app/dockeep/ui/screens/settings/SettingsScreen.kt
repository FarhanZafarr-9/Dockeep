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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ColumnScope
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
    var showHidden by mainVM.showHidden
    var compactList by mainVM.compactList

    val contentPathLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            mainVM.setContentPathUri(result)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }) { paddingValues ->
        BackHandler {
            onGoBack()
        }

        val context = LocalContext.current
        var openThemeDialog by remember { mutableStateOf(false) }
        val themeOptions = listOf(ThemeMode.AUTO, ThemeMode.DARK, ThemeMode.LIGHT)
        val uriHandler = LocalUriHandler.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Files & Storage") {
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
                    trailing = {
                        Switch(
                            checked = deleteOrig,
                            onCheckedChange = { checked -> mainVM.setDeleteOriginal(checked) }
                        )
                    }
                )
                SettingsItem(
                    title = "Show hidden files",
                    subtitle = "Display files starting with a dot",
                    icon = Icons.Outlined.Info,
                    trailing = {
                        Switch(
                            checked = showHidden,
                            onCheckedChange = { checked -> mainVM.setShowHidden(checked) }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Appearance") {
                SettingsItem(
                    title = "App theme",
                    subtitle = "Switch between light, dark, or system default",
                    icon = Icons.Outlined.Palette,
                    onClick = { openThemeDialog = true }
                )
                SettingsItem(
                    title = "Compact list",
                    subtitle = "Use smaller list items for files",
                    icon = Icons.Outlined.Settings,
                    trailing = {
                        Switch(
                            checked = compactList,
                            onCheckedChange = { checked -> mainVM.setCompactList(checked) }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "About & Support") {
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
                    subtitle = "Send feedback, report issues, or ask questions",
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
                    subtitle = "Learn how your data is collected and used",
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

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Version ${context.getAppVersion()?.versionName ?: ""}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

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

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
    trailing: @Composable (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodyMedium) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp)
    )
}
