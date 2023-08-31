package xyz.lucasteel.kitme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForgotPasswordScreenViewModel : ViewModel() {
    val otpValue = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val isResendAvailable = mutableStateOf(true)
    val millisecondsLeftUntilAvailable = mutableStateOf(0)
    val username = mutableStateOf("")
    val newPassword = mutableStateOf("")
    val confirmNewPassword = mutableStateOf("")
    val isOtpSendLoading = mutableStateOf(false)
    val isNewPasswordVisible = mutableStateOf(false)
    val isConfirmPasswordVisible = mutableStateOf(false)

    fun startSecondsCountDown(){
        MainScope().launch {
            isResendAvailable.value = false
            millisecondsLeftUntilAvailable.value = 30000
            while(millisecondsLeftUntilAvailable.value > 0){
                millisecondsLeftUntilAvailable.value -= 1000
                delay(1000)
            }
            isResendAvailable.value = true
        }
    }

    fun arePasswordsMatching(): Boolean{
        return newPassword.value == confirmNewPassword.value
    }
}