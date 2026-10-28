package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.paulodiogo.orcamento.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    padding: PaddingValues,
    busy: Boolean,
    message: String?,
    lastBackupName: String?,
    onBack: () -> Unit,
    folderSelected: Boolean,
    backups: List<br.com.paulodiogo.orcamento.data.AvailableBackup>,
    selectedGeneration: String?,
    onCreate: () -> Unit,
    onRestore: () -> Unit,
    onChooseBackupFolder: () -> Unit,
    onSelectBackup: (String) -> Unit,
) {
    ScreenContent(
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = padding.calculateBottomPadding(),
            start = padding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            end = padding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
        ),
        horizontalPadding = 0.dp,
        scrollable = true,
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.backup_title), maxLines = 1) },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    enabled = !busy,
                    modifier = Modifier.padding(start = 4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_nav_back),
                        contentDescription = stringResource(R.string.backup_back),
                    )
                }
            },
        )
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
            message?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(it, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(16.dp))
                }
            }
            Text(stringResource(R.string.backup_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.backup_security_notice),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
            Button(onClick = onCreate, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.backup_create)) }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (backups.isEmpty()) stringResource(R.string.backup_no_backups_title) else stringResource(R.string.backup_available_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        if (backups.isEmpty()) stringResource(R.string.backup_none_available) else stringResource(R.string.backup_available_count, backups.size),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!folderSelected) Text(stringResource(R.string.backup_destination_required), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onChooseBackupFolder, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (folderSelected) "Escolher outra pasta de backups" else "Selecionar pasta existente para restaurar")
            }
            backups.forEach { backup ->
                androidx.compose.material3.TextButton(
                    onClick = { onSelectBackup(backup.generationId) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (selectedGeneration == backup.generationId) "✓ ${backup.displayName}" else backup.displayName,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Button(onClick = onRestore, enabled = !busy && selectedGeneration != null, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.backup_restore)) }
            if (busy) CircularProgressIndicator()
        }
    }
}
