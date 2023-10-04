package xyz.lucasteel.kitme

import android.content.Context
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
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
    val isBottomSheetVisible = mutableStateOf(false)

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
                    val tempPostArray = ArrayList<Document>()

                    for (currentPostOID in postOIDList) {
                        val currentPostData =
                            getPostInfo(postOID = currentPostOID.toString(), scope = MainScope())
                        if (currentPostData.contains("{")) {
                            val currentPostDocument = Document.parse(currentPostData)
                            tempPostArray.add(currentPostDocument)
                        } else {
                            snackbarHostState.showSnackbar("Error: $currentPostData")
                            isLoading.value = false
                        }
                    }
                    println(tempPostArray)
                    userPostsList.value.addAll(tempPostArray)

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
                        userCommentList.value.add(Document.parse(commentInfo))
                    } else {
                        snackbarHostState.showSnackbar("Error: $commentInfo loading comments WHILE PARSING ARRAY.")
                    }
                }
            } else {
                snackbarHostState.showSnackbar("Error: $unparsedCommentArray loading comments intitally.")
            }
        } catch (e: Exception){
            snackbarHostState.showSnackbar("Error: ${e.message} loading comments ERROR.")
        }
    }
}