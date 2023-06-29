package com.bink.wallet.scenes.prev_updates

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.model.Releases
import com.bink.wallet.utils.ThemeHelper
import com.bink.wallet.utils.firebase.FirebaseRepository
import com.bink.wallet.utils.firebase.releaseNotes
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrevUpdatesViewModel(
    private val dataStoreSource: DataStoreSourceImpl,
    private val firebaseRepository: FirebaseRepository,
) : BaseViewModel() {

    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
        get() = _theme

    private val _previousUpdatesUiState = MutableStateFlow(PreviousUpdatesUiState())
    val previousUpdatesUiState: StateFlow<PreviousUpdatesUiState> = _previousUpdatesUiState.asStateFlow()

    init {
        getSelectedTheme()
        getPreviousUpdates()
    }

    private fun getSelectedTheme() {
        viewModelScope.launch {
            dataStoreSource.getCurrentlySelectedTheme().collect {
                _theme.value = it
            }
        }
    }

    private fun getPreviousUpdates() {
        _previousUpdatesUiState.update {
            it.copy(loading = true)
        }

        firebaseRepository.getDocument<Releases>(Firebase.releaseNotes().whereEqualTo("platform", "Android")) { releaseNotes ->
            releaseNotes?.let {
                _previousUpdatesUiState.update {
                    it.copy(releaseNotes = releaseNotes, loading = false)
                }
            } ?: run {
                _previousUpdatesUiState.update {
                    it.copy(loading = false)
                }
            }

        }
    }
}

data class PreviousUpdatesUiState(val releaseNotes: Releases? = null, val loading: Boolean = false)