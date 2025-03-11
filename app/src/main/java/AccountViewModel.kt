package com.example.loginpage

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AccountHolder(val name: String, val branch: String)

class AccountViewModel : ViewModel() {
    private val _accountHolder = MutableStateFlow<AccountHolder?>(null)
    val accountHolder: StateFlow<AccountHolder?> = _accountHolder

    fun setAccount(account: AccountHolder) {
        _accountHolder.value = account
    }
}