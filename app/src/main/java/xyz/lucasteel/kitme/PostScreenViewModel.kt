package xyz.lucasteel.kitme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bson.Document
import org.bson.types.ObjectId
import xyz.lucasteel.kitme.logic.getCommentInfo
import xyz.lucasteel.kitme.logic.getPostInfo

class PostScreenViewModel : ViewModel(), PostComposableInterface {
    val isLoading = mutableStateOf(false)
    val token = mutableStateOf("")
    val username = mutableStateOf("")
    val title = mutableStateOf("")
    val postOID = mutableStateOf(ObjectId())
    val postLikes = mutableStateOf(0)
    val resource = mutableStateOf("")
    val ownerOID = mutableStateOf(ObjectId())
    val postingDate = mutableStateOf("")
    var commentsList = mutableListOf<Document>()
    val commentContent = mutableStateOf("")

    private val _isLoading = MutableStateFlow(false)
    val isSwipeLoading = _isLoading.asStateFlow()

    fun setToken(s: String) {
        token.value = s
    }

    override fun getToken(): String {
        return token.value
    }

    fun getPostInfo(snackbarHostState: SnackbarHostState, givenPostOID: String) {
        MainScope().launch {
            try {
                isLoading.value = true
                val unparsedPostDocument = getPostInfo(postOID = givenPostOID, scope = MainScope())

                if (!unparsedPostDocument.contains("{")) {
                    snackbarHostState.showSnackbar("ERROR: $unparsedPostDocument")
                } else {
                    val postDocument = Document.parse(unparsedPostDocument)
                    username.value = postDocument["owner"] as String
                    title.value = postDocument["title"] as String
                    postOID.value = postDocument["_id"] as ObjectId
                    postLikes.value = postDocument["likes"] as Int
                    resource.value = postDocument["resource"] as String
                    ownerOID.value = postDocument["ownerOID"] as ObjectId
                    postingDate.value = postDocument["postingDate"] as String

                    loadComments(
                        snackbarHostState = snackbarHostState,
                        postsList = postDocument.getList("comments", ObjectId().javaClass)
                    )
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("ERROR: ${e.message}")
                println("Error: ${e.message}")
                isLoading.value = false
                _isLoading.value = false
            }
        }
    }

   private suspend fun loadComments(snackbarHostState: SnackbarHostState, postsList: List<ObjectId>) {
       try {
           if(postsList.size > commentsList.size) {
               for (commentID in postsList) {
                   val commentContent =
                       getCommentInfo(commentOID = commentID.toString(), scope = MainScope())
                   println(commentContent)
                   val commentDocument = Document.parse(commentContent)
                   if(commentsList.size != 0){
                       commentsList.clear()
                   }
                   commentsList.add(commentDocument)
               }
           }
           isLoading.value = false
           _isLoading.value = false
       } catch(e: Exception) {
           snackbarHostState.showSnackbar("Error: ${e.message}")
           println("Error in loadComments: ${e.message}")
           isLoading.value = false
           _isLoading.value = false
       }
    }

    fun refreshPost(snackbarHostState: SnackbarHostState, postOID: String){
        viewModelScope.launch {
            _isLoading.value = true
            getPostInfo(snackbarHostState, postOID)
        }
    }
}