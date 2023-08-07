package xyz.lucasteel.kitme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ForgotUsernameScreenViewModel : ViewModel() {

    var emailText = mutableStateOf("")
    var isEmailError = mutableStateOf(false)
    var emailTip = mutableStateOf("")
    var isLoading = mutableStateOf(false)

    fun checkEmail(){
        if(!emailText.value.contains("@") || !emailText.value.contains(".")){
            isEmailError.value = true
            emailTip.value = "Please provide a valid email address."
        }else{
            isEmailError.value = false
        }
    }
}