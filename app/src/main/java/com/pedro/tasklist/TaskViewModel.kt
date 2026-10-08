package com.pedro.tasklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Cada estado mostra uma interface diferente.
sealed interface UiState {
    data object Loading : UiState
    data object Empty : UiState
    data class Content(val tasks: List<Task>) : UiState
    data class Error(val message: String) : UiState
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    private val mutableState = MutableStateFlow<UiState>(UiState.Loading)
    val state = mutableState.asStateFlow()

    init {
        // Observa os dados: quando o Repository salva, a tela recebe a lista nova.
        viewModelScope.launch {
            repository.tasks.collect { tasks ->
                mutableState.value = if (tasks.isEmpty()) UiState.Empty else UiState.Content(tasks)
            }
        }
        retry()
    }

    fun retry() {
        viewModelScope.launch {
            mutableState.value = UiState.Loading
            try {
                repository.load()
                // Também atualiza quando o arquivo retorna uma lista igual à anterior.
                val tasks = repository.tasks.value
                mutableState.value = if (tasks.isEmpty()) UiState.Empty else UiState.Content(tasks)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableState.value = UiState.Error("Não foi possível ler as tarefas. Tente novamente.")
            }
        }
    }

    fun create(title: String, description: String?, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.save(title.trim(), description?.trim()?.ifBlank { null })
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError()
            }
        }
    }
}
