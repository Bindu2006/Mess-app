package com.example.myapplication.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns

class LoginViewModel : ViewModel() {
    
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun validateAndLogin(email: String, password: String) {
        when {
            !isEmailValid(email) -> {
                _loginState.value = LoginState.InvalidEmail
            }
            !isPasswordValid(password) -> {
                _loginState.value = LoginState.InvalidPassword
            }
            else -> {
                // TODO: Implement actual authentication
                performLogin(email, password)
            }
        }
    }

    private fun performLogin(email: String, password: String) {
        // Simulate network call
        if (email == "admin@mess.com" && password == "admin123") {
            _loginState.value = LoginState.Success
        } else {
            _loginState.value = LoginState.Error
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    sealed class LoginState {
        object Success : LoginState()
        object InvalidEmail : LoginState()
        object InvalidPassword : LoginState()
        object Error : LoginState()
    }
} 