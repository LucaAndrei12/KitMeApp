package xyz.lucasteel.kitme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.lucasteel.kitme.logic.getSavedPosts

class SavedScreenViewModel : ViewModel(), PostComposableInterface  {
    val savedPostsList = mutableStateOf(ArrayList<String>())
    val token = mutableStateOf("")
    private val _isRefreshLoading = MutableStateFlow(false)
    val isRefreshLoading = _isRefreshLoading.asStateFlow()

    fun getSaved(snackbarHostState: SnackbarHostState){
        MainScope().launch {
            val listToDecide = getSavedPosts(MainScope(), token.value)
            if(listToDecide == null){
                savedPostsList.value = ArrayList()
                snackbarHostState.showSnackbar("An error occurred or no saved posts.")
            } else {
                savedPostsList.value = listToDecide
            }
        }
    }

    override fun getToken(): String{
        return token.value
    }

    fun refreshPosts(snackbarHostState: SnackbarHostState){
        viewModelScope.launch {
            _isRefreshLoading.value = true
            getSaved(snackbarHostState)
            _isRefreshLoading.value = false
        }
    }
}