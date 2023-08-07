package xyz.lucasteel.kitme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VerifyOTPScreenViewModel : ViewModel()  {

    var otpText = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var isResendAvailable = mutableStateOf(true)
    var millisecondsLeftUntilAvailable = mutableStateOf(0)

    fun startStateResendCountDown(){
        MainScope().launch {
            isResendAvailable.value = false
            delay(30000)
            isResendAvailable.value = true
        }
    }

    fun startSecondsCountDown(){
        MainScope().launch {
            millisecondsLeftUntilAvailable.value = 30000
            while(millisecondsLeftUntilAvailable.value > 0){
                millisecondsLeftUntilAvailable.value -= 1000
                delay(1000)
            }
        }
    }
}