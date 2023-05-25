package com.bink.wallet.scenes.polls

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.model.PollItem
import com.bink.wallet.model.PollResultItem
import com.bink.wallet.model.PollResultSummary
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.settings.UserRepository
import com.bink.wallet.utils.ThemeHelper
import com.bink.wallet.utils.firebase.FirebaseRepository
import com.bink.wallet.utils.firebase.getTime
import com.bink.wallet.utils.firebase.pollResults
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class PollsViewModel(private val dataStoreSource: DataStoreSourceImpl, private val firebaseRepository: FirebaseRepository, private val userRepository: UserRepository) : BaseViewModel() {

    private val _selectedAnswerUiState = MutableStateFlow(AnswerUiState())
    val selectedAnswerUiState: StateFlow<AnswerUiState> = _selectedAnswerUiState.asStateFlow()

    private val _answerResultUiState = MutableStateFlow(ResultUiState())
    val answerResultUiState: StateFlow<ResultUiState> = _answerResultUiState.asStateFlow()

    var poll = MutableLiveData<PollItem>()

    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
        get() = _theme

    init {
        getSelectedTheme()
    }

    private fun getSelectedTheme() {
        viewModelScope.launch {
            dataStoreSource.getCurrentlySelectedTheme().collect {
                _theme.value = it
            }
        }
    }

    fun answerSelected(answer: String) {
        _selectedAnswerUiState.update {
            it.copy(selectedAnswer = answer)
        }
    }

    fun submitAnswer() {
        getUser { user ->
            _answerResultUiState.update {
                it.copy(loading = true)
            }

            val documentId = UUID.randomUUID().toString()

            val pollAnswer = PollResultItem(
                answer = _selectedAnswerUiState.value.selectedAnswer,
                createdDate = getTime(),
                pollId = poll.value?.id ?: "",
                userId = user.uid
            )

            firebaseRepository.setDocument(id = documentId, document = pollAnswer, collection = Firebase.pollResults()) { success ->
                if (success) {
                    getResultSummary()
                } else {
                    _selectedAnswerUiState.update {
                        it.copy(error = "An unexpected error has occurred, please try again later.")
                    }

                    _answerResultUiState.update {
                        it.copy(loading = false)
                    }
                }
            }
        }
    }

    private fun getResultSummary() {
        val userAnswer = _selectedAnswerUiState.value.selectedAnswer

        firebaseRepository.getCollection<PollResultItem>(Firebase.pollResults().whereEqualTo("pollId", poll.value?.id)) { results ->

            val pollResultSummary = poll.value?.answers?.map { answer ->
                val answerResults = results?.filter { it.answer == answer }
                val percentage = (answerResults?.size?.toFloat() ?: 0f) / (results?.size?.toFloat() ?: 1f) * 100
                PollResultSummary(answer, percentage, answer == userAnswer)
            }.orEmpty().toMutableList()

            _answerResultUiState.update {
                it.copy(loading = false, pollResultSummary = pollResultSummary)
            }
        }
    }

    private fun getUser(userCallback: (User) -> Unit) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserDetails()
                userCallback(user)
            } catch (e: Exception) {
                _selectedAnswerUiState.update {
                    it.copy(error = "An unexpected error has occurred, please try again later.")
                }
            }
        }
    }

}

data class AnswerUiState(val selectedAnswer: String = "", val error: String = "")

data class ResultUiState(val loading: Boolean = false, val pollResultSummary: MutableList<PollResultSummary> = arrayListOf())