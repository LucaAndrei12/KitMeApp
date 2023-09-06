package xyz.lucasteel.kitme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.bson.Document
import org.bson.types.ObjectId
import xyz.lucasteel.kitme.logic.getUser
import xyz.lucasteel.kitme.ui.theme.justFamily

@Composable
fun SavedScreen(token: String, navController: NavController) {
    val viewModel: SavedScreenViewModel = viewModel()
    viewModel.token.value = token
    val savedScreenSnackBarHost = remember { SnackbarHostState() }
    val isRefreshLoading by viewModel.isRefreshLoading.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshLoading)
    viewModel.getSaved(snackbarHostState = savedScreenSnackBarHost)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refreshPosts(savedScreenSnackBarHost) }) {
        SavedScreenContent(
            navController = navController,
            viewModel = viewModel,
            snackbarHostState = savedScreenSnackBarHost
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreenTopBar() {
    TopAppBar(title = {
        Text(
            text = "Your saved posts",
            fontFamily = justFamily,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 5.dp)
        )
    })
}

@Composable
fun SavedScreenContent(
    navController: NavController,
    viewModel: SavedScreenViewModel,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = { SavedScreenTopBar() },
        bottomBar = { BottomNavigation(navController = navController) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
        Surface(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (viewModel.savedPostsList.value.size == 0) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Looks like you haven't saved anything yet!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = justFamily,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "P.S. Pull down to refresh.",
                        fontFamily = justFamily,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        items(items = viewModel.savedPostsList.value,
                            key = { post -> (Document.parse(post)["_id"] as ObjectId).toString() }) { postString ->
                            val postDocument = Document.parse(postString)
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
                                isOnHomePage = true,
                                isSavedDefault = true
                            )
                        }
                    })
            }
        }
    }
}

@Preview
@Composable
fun SavedPostsPreview() {
    SavedScreenContent(
        navController = rememberNavController(),
        viewModel = SavedScreenViewModel(),
        snackbarHostState = remember { SnackbarHostState() })
}
