package xyz.lucasteel.kitme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.bson.Document
import xyz.lucasteel.kitme.logic.getPostInfo
import xyz.lucasteel.kitme.logic.getUser
import xyz.lucasteel.kitme.logic.search

class SearchScreenViewModel : ViewModel(), PostComposableInterface  {
    val textQuery = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val isPostSearch = mutableStateOf(false)
    val token = mutableStateOf("")
    val foundPostsList = mutableStateOf(ArrayList<Document>())
    val foundUsersList = mutableStateOf(ArrayList<Document>())

    fun updateWithSearch(snackbarHostState: SnackbarHostState, isPostSearch: Boolean, query: String){
        MainScope().launch {
            try {
                if(isPostSearch) foundPostsList.value.clear() else foundUsersList.value.clear()
                isLoading.value = true
                val searchResult = search(isPostSearch = isPostSearch, query = query, scope = MainScope())
                if (searchResult.contains("queryResult")) {
                    val resultDocument = Document.parse(searchResult)
                    val oidList = resultDocument.getList("queryResult", String::class.java)
                    for (oid in oidList){
                        if(isPostSearch){
                            val postDocument = getPostInfo(postOID = oid.toString(), scope = MainScope())
                            foundPostsList.value.add(Document.parse(postDocument))
                        } else {
                            val userDocument = getUser(userID = oid.toString(), scope = MainScope())
                            foundUsersList.value.add(Document.parse(userDocument))
                        }
                    }
                    println("POST FOUND: ${foundPostsList.value}")
                    println("USER FOUND: ${foundUsersList.value}")
                    println("IS POST SEARCH: $isPostSearch")
                    isLoading.value = false
                } else {
                    isLoading.value = false
                    snackbarHostState.showSnackbar("Error: $searchResult")
                }
            } catch (e: Exception){
                isLoading.value = false
                snackbarHostState.showSnackbar("Error: ${e.message}")
            }
        }
    }

    override fun getToken(): String {
        return token.value
    }
}