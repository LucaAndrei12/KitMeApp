package xyz.lucasteel.kitme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SignUpScreenViewModel : ViewModel()  {

    var usernameText = mutableStateOf("")
    var emailText = mutableStateOf("")
    var passwordText = mutableStateOf("")
    var confirmPasswordText = mutableStateOf("")
    var isConfirmPasswordVisible = mutableStateOf(false)
    var isPasswordVisible = mutableStateOf(false)

    var isPasswordError = mutableStateOf(false)
    var passwordTip = mutableStateOf("")

    var isEmailError = mutableStateOf(false)
    var emailTip = mutableStateOf("")

    var isUsernameError = mutableStateOf(false)
    var usernameTip = mutableStateOf("")

    var isLoading = mutableStateOf(false)


    fun checkUsername(){
        if(usernameText.value.contains("@") || usernameText.value.contains("\"") || usernameText.value.contains("/") || usernameText.value.length > 30){
            isUsernameError.value = true
            usernameTip.value = "Usernames cannot contain \"@\", \"\\\", \"/\" or be longer than 30 characters."
        } else {
            isUsernameError.value = false
        }
    }

    fun checkPasswords(){
        if(passwordText.value != confirmPasswordText.value){
            isPasswordError.value = true
            passwordTip.value = "The passwords do not match."
        } else if(passwordText.value.length < 8){
            isPasswordError.value = true
            passwordTip.value = "Passwords must have at least 8 characters."
        } else {
            isPasswordError.value = false
        }
    }

    fun checkEmail(){
        if(!emailText.value.contains("@") || !emailText.value.contains(".")){
            isEmailError.value = true
            emailTip.value = "Please provide a valid email address."
        }else{
            isEmailError.value = false
        }
    }

    fun areAllStatesGood():Boolean{
        if(!isUsernameError.value && !isEmailError.value && !isPasswordError.value){
            return true
        }
        return false
    }
}