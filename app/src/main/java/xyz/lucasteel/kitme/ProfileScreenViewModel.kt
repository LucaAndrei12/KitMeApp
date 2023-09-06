package xyz.lucasteel.kitme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bson.Document
import org.bson.types.ObjectId
import xyz.lucasteel.kitme.logic.getPostInfo
import xyz.lucasteel.kitme.logic.getUser

class ProfileScreenViewModel : ViewModel(), PostComposableInterface {
    val userDocument = mutableStateOf(Document())
    val isLoading = mutableStateOf(true)
    val userPostList = mutableStateOf(ArrayList<Document>())
    val token = mutableStateOf("")

    override fun getToken(): String {
        return token.value
    }

    fun setToken(s: String) {
        token.value = s
    }

    suspend fun loadData(snackbarHostState: SnackbarHostState, userOID: String){
        val userJson = getUser(scope = MainScope(), userID = userOID)
        var userDocument: Document
        try{
            userDocument = Document.parse(userJson)
            this.userDocument.value = userDocument
        } catch (e: Exception){
            userDocument = Document()
            snackbarHostState.showSnackbar("Error: ${e.message}")
            isLoading.value = false
        }

        try {
            val postOIDList = userDocument.getList("posts", ObjectId().javaClass)
            val tempPostArray = ArrayList<Document>()

            for(currentPostOID in postOIDList){
                val currentPostData = getPostInfo(postOID = currentPostOID.toString(), scope = MainScope())
                if(currentPostData.contains("{")){
                    val currentPostDocument = Document.parse(currentPostData)
                    tempPostArray.add(currentPostDocument)
                } else {
                    snackbarHostState.showSnackbar("Error: $currentPostData")
                    isLoading.value = false
                }
            }
            println(tempPostArray)
            this.userPostList.value.addAll(tempPostArray)
            isLoading.value = false
            println("FINAL ARRAY COMMITED: ${this.userPostList.value}")

        } catch (e: Exception){
            snackbarHostState.showSnackbar("Error: ${e.message}")
            isLoading.value = false
        }
    }

/*
    fun getUserData(snackbarHostState: SnackbarHostState, userOID: String) {
        MainScope().launch(Dispatchers.IO) {
            lock.withLock {
                val getUserResponse = getUser(userID = userOID, scope = MainScope())
                println("PARSED USER: ${Document.parse(getUserResponse)}")

                if (getUserResponse.contains("{")) {
                    userDocument.value = Document.parse(getUserResponse)
                    isLoading.value = false
                } else {
                    snackbarHostState.showSnackbar("An error occurred")
                    isLoading.value = false
                }
            }
        }
    }

    fun addPostsToList(snackbarHostState: SnackbarHostState) {
        MainScope().launch(Dispatchers.IO) {
            lock.withLock {
                isLoading.value = true
                val postOIDArray: List<ObjectId> = userDocument.value.getList("posts", ObjectId().javaClass)

                println("POST LIST: $postOIDArray")

                for (a in postOIDArray) {
                    println("POST LIST in for: $postOIDArray")
                    val currentPostString = getPostInfo(a.toString(), MainScope())
                    println(currentPostString)

                    if (currentPostString.contains("{")) {
                        val currentPostDocument = Document.parse(currentPostString)
                        userPostList.value.add(currentPostDocument)
                    } else {
                        snackbarHostState.showSnackbar("An error occurred.")
                    }
                }
                isLoading.value = false
            }
        }
    }

 */
}