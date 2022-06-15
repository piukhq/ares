package com.bink.wallet.scenes.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.DeleteRequest
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.launch

class DeleteAccountViewModel(var userRepository: UserRepository) : BaseViewModel() {

    private val _userDeleted = MutableLiveData<Boolean>()
    val userDeleted: LiveData<Boolean>
        get() = _userDeleted

    private val _deleteError = MutableLiveData<Exception>()
    val deleteError: LiveData<Exception>
        get() = _deleteError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading


    init {
        deleteUser()
    }

    private fun deleteUser() {
        val email = LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL) ?: ""
        val timestamp = System.currentTimeMillis()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.deleteUser(DeleteRequest(email, timestamp))
                _isLoading.value = false
                _userDeleted.value = true
            } catch (e: Exception) {
                _isLoading.value = false
                _deleteError.value = e
            }
        }
    }

}