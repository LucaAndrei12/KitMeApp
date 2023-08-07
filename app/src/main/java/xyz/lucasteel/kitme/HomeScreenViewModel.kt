package xyz.lucasteel.kitme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeScreenViewModel : ViewModel(){

        val isLoading = mutableStateOf(false)
        val isUpdateLoading = mutableStateOf(false)
        private var token = ""
        var postsList = mutableStateOf(ArrayList<String>())

        fun setToken(value: String){
                token = value
        }

        fun getToken(): String{
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