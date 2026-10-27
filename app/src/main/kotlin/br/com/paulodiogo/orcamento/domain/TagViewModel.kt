package br.com.paulodiogo.orcamento.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.TagEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TagState {
    data object Loading : TagState
    data class Content(val tags: List<TagEntity>) : TagState
    data object Empty : TagState
    data class Error(val message: String) : TagState
}

class TagViewModel(private val database: BudgetDatabase) : ViewModel() {
    private val operations = TagOperations(database)
    private val _state = MutableStateFlow<TagState>(TagState.Loading)
    val state: StateFlow<TagState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            runCatching { database.tagDao().findAll() }
                .onSuccess { tags -> _state.value = if (tags.isEmpty()) TagState.Empty else TagState.Content(tags) }
                .onFailure { _state.value = TagState.Error(it.message ?: "Não foi possível carregar as tags") }
        }
    }

    fun save(name: String, editingId: String? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                if (editingId == null) operations.createOrReuse(name) else operations.rename(editingId, name)
            }.onSuccess { refresh(); onComplete() }
                .onFailure { _state.value = TagState.Error(it.message ?: "Não foi possível salvar a tag") }
        }
    }

    fun delete(id: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { operations.delete(id) }
                .onSuccess { refresh(); onComplete() }
                .onFailure { _state.value = TagState.Error(it.message ?: "Não foi possível excluir a tag") }
        }
    }

    class Factory(private val database: BudgetDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TagViewModel(database) as T
    }
}
