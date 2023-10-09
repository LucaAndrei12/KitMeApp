package xyz.lucasteel.kitme

import android.content.Context
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
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
import xyz.lucasteel.kitme.logic.getUser
import xyz.lucasteel.kitme.logic.getUserID
import xyz.lucasteel.kitme.logic.getUsersComments

class UserScreenViewModel : ViewModel(), PostComposableInterface {

    val userDocument = mutableStateOf(Document())
    val token = mutableStateOf("")
    val userPostsList = mutableStateOf(ArrayList<Document>())
    val userCommentList = mutableStateOf(ArrayList<Document>())
    val arePostsSelected = mutableStateOf(true)
    val isLoading = mutableStateOf(true)
    val updatePFPUri = mutableStateOf(Uri.EMPTY)

    private val _isLoading = MutableStateFlow(false)
    val isSwipeLoading = _isLoading.asStateFlow()

    override fun getToken(): String {
        return token.value
    }

    fun setToken(s: String) {
        token.value = s

    }

    fun loadData(snackbarHostState: SnackbarHostState, token: String) {
        setToken(token)
        MainScope().launch(Dispatchers.IO) {

            val userOID = getUserID(token, scope = MainScope())
            if (userOID == "false") {
                snackbarHostState.showSnackbar("Error while fetching the user.")
            } else {
                val userJson = getUser(userID = userOID, scope = MainScope())
                var tempUserDocument: Document
                try {
                    tempUserDocument = Document.parse(userJson)
                    userDocument.value = tempUserDocument
                } catch (e: Exception) {
                    tempUserDocument = Document()
                    snackbarHostState.showSnackbar("Error: ${e.message} in getting post.")
                    isLoading.value = false
                }

                try {
                    val postOIDList = tempUserDocument.getList("posts", ObjectId().javaClass)

                    for (currentPostOID in postOIDList) {
                        val currentPostData =
                            getPostInfo(postOID = currentPostOID.toString(), scope = MainScope())
                        if (currentPostData.contains("{")) {
                            val currentPostDocument = Document.parse(currentPostData)
                            if (!userPostsList.value.contains(currentPostDocument)) {
                                userPostsList.value.add(currentPostDocument)
                            }
                        } else {
                            snackbarHostState.showSnackbar("Error: $currentPostData")
                            isLoading.value = false
                        }
                    }

                    loadUsersComments(snackbarHostState, userOID = userOID)
                    isLoading.value = false
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Error: ${e.message} while loading from server.")
                    isLoading.value = false
                }
            }
        }
    }

    private suspend fun loadUsersComments(snackbarHostState: SnackbarHostState, userOID: String) {
        try {
            val unparsedCommentArray = getUsersComments(userOID = userOID, scope = MainScope())
            if (unparsedCommentArray.contains("{")) {
                val parseableString =
                    unparsedCommentArray.substring(2, unparsedCommentArray.length - 2)
                val commentIDArray = parseableString.split(",")

                for (commentOID in commentIDArray) {
                    val commentInfo = getCommentInfo(commentOID = commentOID, scope = MainScope())
                    if (commentInfo.contains("{")) {
                        val tempCommentDoc = Document.parse(commentInfo)
                        if (!userCommentList.value.contains(tempCommentDoc)) {
                            userCommentList.value.add(tempCommentDoc)
                        }
                    } else {
                        snackbarHostState.showSnackbar("Error: $commentInfo loading comments WHILE PARSING ARRAY.")
                        _isLoading.value = false

                    }
                }
                _isLoading.value = false

            } else {
                snackbarHostState.showSnackbar("Error: $unparsedCommentArray loading comments intitally.")
                _isLoading.value = false
            }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Error: ${e.message} loading comments ERROR.")
            _isLoading.value = false
        }
    }

    fun refreshPost(snackbarHostState: SnackbarHostState){
        viewModelScope.launch {
            _isLoading.value = true
            loadData(snackbarHostState = snackbarHostState, token = token.value)
        }
    }
}