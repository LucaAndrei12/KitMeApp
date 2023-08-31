package xyz.lucasteel.kitme

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.MainScope
import java.net.URI

class AddPostScreenViewModel  : ViewModel() {
    val postTitle = mutableStateOf("")
    val imageUri = mutableStateOf(Uri.EMPTY)
    val hasSelectedImage = mutableStateOf(false)
    val isSubmitLoading = mutableStateOf(false)
}