package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paulodiogo.orcamento.domain.TagState
import br.com.paulodiogo.orcamento.domain.TagViewModel

@Composable
fun TagManagementScreen(viewModel: TagViewModel, padding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingName by rememberSaveable { mutableStateOf("") }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    ScreenContent(padding, horizontalPadding = 20.dp) {
        ScreenTitle("Tags", "Organize seus gastos por categoria")
        Button(onClick = { editingId = null; editingName = ""; showEditor = true }, modifier = Modifier.fillMaxWidth()) { Text("Criar nova tag") }
        when (val current = state) {
            TagState.Loading -> CircularProgressIndicator()
            TagState.Empty -> EmptyState("Nenhuma tag criada", "Crie categorias como alimentação, transporte ou casa.")
            is TagState.Error -> Text(current.message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            is TagState.Content -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(current.tags, key = { it.id }) { tag ->
                    SummaryCard(tag.name, "Categoria de gastos", "Nome normalizado: ${tag.normalizedName}", Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingId = tag.id; editingName = tag.name; showEditor = true }) { Text("Editar") }
                        TextButton(onClick = { deleteId = tag.id }) { Text("Excluir") }
                    }
                }
            }
        }
    }
    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editingId == null) "Nova tag" else "Editar tag") },
            text = { OutlinedTextField(editingName, { editingName = it }, label = { Text("Nome") }, singleLine = true) },
            confirmButton = { Button(onClick = { viewModel.save(editingName, editingId) { showEditor = false } }) { Text("Salvar") } },
            dismissButton = { TextButton({ showEditor = false }) { Text("Cancelar") } },
        )
    }
    deleteId?.let { id ->
        AlertDialog(onDismissRequest = { deleteId = null }, title = { Text("Excluir tag?") }, text = { Text("As associações desta tag serão removidas dos lançamentos.") }, confirmButton = { Button(onClick = { viewModel.delete(id) { deleteId = null } }) { Text("Excluir") } }, dismissButton = { TextButton({ deleteId = null }) { Text("Cancelar") } })
    }
}
