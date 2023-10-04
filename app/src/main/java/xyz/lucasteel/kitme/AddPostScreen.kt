package xyz.lucasteel.kitme

import android.os.Build
import android.os.FileUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okio.ByteString.Companion.readByteString
import org.apache.commons.io.IOUtils
import xyz.lucasteel.kitme.logic.addPost
import xyz.lucasteel.kitme.ui.theme.justFamily

@Composable
fun AddPostScreen(token: String, navController: NavController) {
    val viewModel = AddPostScreenViewModel()
    AddPostScreenContent(navController = navController, viewModel = viewModel, token = token)
}

@Composable
fun AddPostScreenContent(
    navController: NavController,
    viewModel: AddPostScreenViewModel,
    token: String
) {
    val addPostSnackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AddPostAppBar(navController = navController) },
        snackbarHost = { SnackbarHost(addPostSnackbarHostState) }) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PostTitleTextField(viewModel = viewModel)

                if (!viewModel.hasSelectedImage.value) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        TakePictureButton()
                        Text(
                            text = "OR",
                            fontFamily = justFamily,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        ChoosePictureButton(viewModel = viewModel)
                    }
                } else {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModel.hasSelectedImage.value = false;
                                viewModel.postTitle.value = ""
                            }) {
                            Text("1 selected image", fontFamily = justFamily, modifier = Modifier.padding(start = 10.dp))
                            Icon(imageVector = Icons.Default.Close, contentDescription = "close")
                        }
                        SubcomposeAsyncImage(
                            model = viewModel.imageUri.value,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    if (viewModel.isSubmitLoading.value) {
                        CircularProgressIndicator()
                    } else {
                        SubmitPostButton(
                            viewModel = viewModel,
                            snackbarHostState = addPostSnackbarHostState,
                            navController = navController,
                            token = token
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SubmitPostButton(
    viewModel: AddPostScreenViewModel,
    snackbarHostState: SnackbarHostState,
    token: String,
    navController: NavController
) {
    val context = LocalContext.current
    Button(onClick = {
        if (viewModel.postTitle.value.length <= 50) {
            try {
                MainScope().launch {

                    val fileInputStream = context.contentResolver.openInputStream(viewModel.imageUri.value)
                    val fileContents =  fileInputStream?.readAllBytes()
                    fileInputStream?.close()

                    viewModel.isSubmitLoading.value = true
                    val addPostResponse = addPost(
                        token = token,
                        image = fileContents!!,
                        title = viewModel.postTitle.value,
                        scope = MainScope()
                    )

                    if (addPostResponse == "true") {
                        snackbarHostState.showSnackbar(
                            "Post submitted. Redirecting you...",
                            duration = SnackbarDuration.Short
                        )
                        navController.navigateUp()
                    } else {
                        snackbarHostState.showSnackbar(
                            "An error occurred: $addPostResponse.",
                            duration = SnackbarDuration.Long
                        )
                    }
                    viewModel.isSubmitLoading.value = false
                }
            } catch (e: Exception) {
                MainScope().launch {
                    snackbarHostState.showSnackbar("Error: ${e.message}")
                }
            }
        } else {
            MainScope().launch {
                snackbarHostState.showSnackbar("The title is too long.")
            }
        }
    }) {
        Text(text = "Submit post!", fontFamily = justFamily)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostAppBar(navController: NavController) {
    TopAppBar(title = { Text(text = "Add a post", fontFamily = justFamily) }, navigationIcon = {
        IconButton(
            onClick = { navController.navigateUp() }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back button")
        }
    })
}

@Composable
fun PostTitleTextField(viewModel: AddPostScreenViewModel) {
    TextField(
        value = viewModel.postTitle.value,
        onValueChange = {
            if (viewModel.postTitle.value.length <= 50) {
                viewModel.postTitle.value = it
            }
        },
        label = {
            Text(
                text = "Title(<50 characters)", fontFamily = justFamily
            )
        },
        modifier = Modifier.padding(start = 10.dp, bottom = 5.dp)
    )
}

@Composable
fun TakePictureButton() {
    Button(onClick = { /*TODO in the future*/ }) {
        Text(text = "Take a photo(unavailable)")
        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "camera icon")
    }
}

@Composable
fun ChoosePictureButton(viewModel: AddPostScreenViewModel) {
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.imageUri.value = uri
                viewModel.hasSelectedImage.value = true
                println(uri)
            }
        })

    Button(onClick = {
        singlePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    ) {
        Text(text = "Choose a photo from gallery")
        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "add photo icon")
    }
}