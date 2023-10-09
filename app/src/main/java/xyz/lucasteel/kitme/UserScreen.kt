package xyz.lucasteel.kitme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import xyz.lucasteel.kitme.logic.getToken
import xyz.lucasteel.kitme.ui.theme.justFamily

@Composable
fun UserScreen(navController: NavController) {
    val userScreenViewModel: UserScreenViewModel = viewModel()
    val userScreenSnackbarHost = remember { SnackbarHostState() }
    val isLoading by userScreenViewModel.isSwipeLoading.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    userScreenViewModel.loadData(
        snackbarHostState = userScreenSnackbarHost,
        getToken(LocalContext.current)
    )

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { userScreenViewModel.refreshPost(snackbarHostState = userScreenSnackbarHost) }) {
        Scaffold(snackbarHost = { SnackbarHost(userScreenSnackbarHost) }, bottomBar = {
            BottomNavigation(
                navController = navController
            )
        }) {
            UserScreenContent(
                surfacePadding = it,
                viewModel = userScreenViewModel,
                navController = navController,
                snackbarHostState = userScreenSnackbarHost
            )
        }
    }
}

@Composable
fun UserScreenContent(
    surfacePadding: PaddingValues,
    viewModel: UserScreenViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    Surface(modifier = Modifier.padding(surfacePadding)) {
        if (!viewModel.isLoading.value) {
            Surface(Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfileInfoBrief(
                        username = viewModel.userDocument.value["username"] as String,
                        dateJoined = viewModel.userDocument.value["dateJoined"] as String,
                        pfpLink = viewModel.userDocument.value["profilePicture"] as String,
                        isPFPEditable = true,
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        navController = navController
                    )
                    UnderUserInfoContent(
                        viewModel = viewModel,
                        navController = navController,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UnderUserInfoContent(
    viewModel: UserScreenViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
        TabRow(
            selectedTabIndex = if (viewModel.arePostsSelected.value) 0 else 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Tab(
                selected = viewModel.arePostsSelected.value,
                content = {
                    Text(
                        text = "Posts",
                        fontFamily = justFamily,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                },
                onClick = { viewModel.arePostsSelected.value = true })
            Tab(
                selected = !viewModel.arePostsSelected.value,
                content = {
                    Text(
                        text = "Comments",
                        fontFamily = justFamily,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                },
                onClick = { viewModel.arePostsSelected.value = false })
        }

        AnimatedContent(
            targetState = viewModel.arePostsSelected.value,
            label = "posts/comments"
        ) { arePostsSelected ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (arePostsSelected && viewModel.userPostsList.value.isNotEmpty() || !arePostsSelected && viewModel.userCommentList.value.isNotEmpty())
                    items(items = if (arePostsSelected) viewModel.userPostsList.value else viewModel.userCommentList.value) {
                        if (arePostsSelected) {
                            PostComposable(
                                owner = it["owner"]!! as String,
                                ownerOID = (it["ownerOID"]!! as ObjectId).toString(),
                                title = it["title"]!! as String,
                                datePosted = it["postingDate"]!! as String,
                                resource = it["resource"]!! as String,
                                numberLikes = it["likes"]!! as Int,
                                postOID = (it["_id"]!! as ObjectId).toString(),
                                navController = navController,
                                viewModel = viewModel,
                                snackbarHostState = snackbarHostState,
                                isOwnedByUser = true,
                                isOnHomePage = false,
                                isSavedDefault = false
                            )
                        } else {
                            CommentComposable(
                                commentOID = (it["_id"]!! as ObjectId).toString(),
                                ownerOID = (it["ownerOID"]!! as ObjectId).toString(),
                                likeCount = it["likes"]!! as Int,
                                content = it["content"]!! as String,
                                owner = it["owner"]!! as String,
                                navController = navController,
                                viewModel = viewModel,
                                snackbarHostState = snackbarHostState,
                                isOwnedByUser = true
                            )
                        }
                    } else {
                    item {
                        Text(
                            text = "Nothing to show here :)",
                            fontFamily = justFamily,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreenSheet(viewModel: UserScreenViewModel) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { viewModel.isBottomSheetVisible.value = false },
        sheetState = sheetState
    ) {
        Text(
            text = "Log out",
            color = MaterialTheme.colorScheme.tertiary,
            fontFamily = justFamily,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 5.dp, top = 10.dp, bottom = 10.dp)
        )
        Divider(modifier = Modifier.fillMaxWidth())
    }
}

 */