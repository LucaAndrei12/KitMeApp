package xyz.lucasteel.kitme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel : ViewModel(), PostComposableInterface{

        val isLoading = mutableStateOf(false)
        val isUpdateLoading = mutableStateOf(false)
        private var token = ""
        var postsList = mutableStateOf(ArrayList<String>())
        private val _isRefreshLoading = MutableStateFlow(false)
        val isRefreshLoading = _isRefreshLoading.asStateFlow()

        fun refreshPosts(navController: NavController, snackbarHostState: SnackbarHostState){
                viewModelScope.launch {
                        _isRefreshLoading.value = true
                        getFeed(snackbarHostState, navController)
                        _isRefreshLoading.value = false
                }
        }

        fun setToken(value: String){
                token = value
        }

        override fun getToken(): String{
                return token
        }

        fun getFeed(snackbarHostState: SnackbarHostState, navController: NavController) {
                isLoading.value = true

                MainScope().launch {
                        val getFeedResponse = MainScope().async {
                                xyz.lucasteel.kitme.logic.getFeed(token = getToken(), MainScope())
                        }.await()

                        if(getFeedResponse.size == 1 && getFeedResponse[0].contains("Session expired")){
                                snackbarHostState.showSnackbar("An error occurred: Session Expired. Sending you to login page...")
                                navController.navigate("loginScreen")
                        } else if (getFeedResponse.size == 1){
                                snackbarHostState.showSnackbar("An error occurred: ${getFeedResponse[0]}. Try restarting the app.")
                                isLoading.value = false
                        } else {
                                postsList.value = getFeedResponse
                                isLoading.value = false
                        }
                }
        }

        fun updateWithPosts(snackbarHostState: SnackbarHostState, navController: NavController){
                isUpdateLoading.value = true

                MainScope().launch {
                        val getFeedResponse = MainScope().async {
                                xyz.lucasteel.kitme.logic.getFeed(token = getToken(), MainScope())
                        }.await()

                        delay(500)

                        if(getFeedResponse.size == 1 && getFeedResponse[0].contains("Session expired")){
                                snackbarHostState.showSnackbar("An error occurred: Session Expired. Sending you to login page...")
                                navController.navigate("loginScreen")
                        } else if (getFeedResponse.size == 1){
                                snackbarHostState.showSnackbar("An error occurred: ${getFeedResponse[0]}. Try restarting the app.")
                                isUpdateLoading.value = false
                        } else {
                                postsList.value.addAll(getFeedResponse)
                                isUpdateLoading.value = false
                        }
                }
        }

}