package xyz.lucasteel.kitme

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.bson.Document
import org.bson.types.ObjectId
import xyz.lucasteel.kitme.logic.getUser
import xyz.lucasteel.kitme.ui.theme.justFamily

//ONLY FOR RANDOM USER. FOR APP'S OWNER, USE userScreen
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ProfileScreen(userOID: String, token: String, navController: NavController) {
    val viewModel: ProfileScreenViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.setToken(token)
    MainScope().launch {
        viewModel.loadData(snackbarHostState, userOID)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            ProfileScreenContent(
                viewModel = viewModel,
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Composable
fun ProfileScreenContent(
    viewModel: ProfileScreenViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    val userDoc = viewModel.userDocument.value

    if (!viewModel.isLoading.value) {
        val username = userDoc["username"] as String
        val dateJoined = userDoc["dateJoined"] as String
        val pfpLink = userDoc["profilePicture"] as String

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BackButton(navController = navController)
            ProfileInfoBrief(username = username, dateJoined = dateJoined, pfpLink = pfpLink)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp),
                content = {
                    item {
                        Text(
                            text = "User's posts:",
                            fontFamily = justFamily,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                    items(items = viewModel.userPostList.value) { postDocument ->
                        PostComposable(
                            owner = postDocument["owner"]!! as String,
                            ownerOID = (postDocument["ownerOID"]!! as ObjectId).toString(),
                            title = postDocument["title"]!! as String,
                            datePosted = postDocument["postingDate"]!! as String,
                            resource = postDocument["resource"]!! as String,
                            numberLikes = postDocument["likes"]!! as Int,
                            postOID = (postDocument["_id"]!! as ObjectId).toString(),
                            navController = navController,
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState,
                            isOwnedByUser = false,
                            isOnHomePage = false,
                            isSavedDefault = false
                        )
                    }
                })
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun ProfileInfoBrief(username: String, dateJoined: String, pfpLink: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = pfpLink,
            contentDescription = "user's profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(bottom = 5.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Text(
            text = username,
            fontFamily = justFamily,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(5.dp)
        )
        Text(
            text = "Joined KitMe $dateJoined",
            fontFamily = justFamily,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(5.dp)
        )
        Divider(
            modifier = Modifier
                .padding(start = 15.dp, end = 15.dp)
                .fillMaxWidth()
        )
    }
}

