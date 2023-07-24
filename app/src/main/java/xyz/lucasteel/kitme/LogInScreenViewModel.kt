package xyz.lucasteel.kitme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class LogInScreenViewModel : ViewModel()  {

    var usernameText = mutableStateOf("")
    var passwordText = mutableStateOf("")
    var isPasswordVisible = mutableStateOf(false)
    var captchaToken = mutableStateOf("")
    var token = mutableStateOf("")



}