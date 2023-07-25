package xyz.lucasteel.kitme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogInScreenViewModel : ViewModel()  {

    var usernameText = mutableStateOf("")
    var passwordText = mutableStateOf("")
    var isPasswordVisible = mutableStateOf(false)
    var captchaToken = mutableStateOf("")
    var token = mutableStateOf("")
    var isLoading = mutableStateOf(false)
}