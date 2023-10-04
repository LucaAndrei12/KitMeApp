package xyz.lucasteel.kitme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import xyz.lucasteel.kitme.logic.addComment
import xyz.lucasteel.kitme.logic.likeCommentAction
import xyz.lucasteel.kitme.logic.likePostAction
import xyz.lucasteel.kitme.logic.removeComment
import xyz.lucasteel.kitme.logic.removePost
import xyz.lucasteel.kitme.ui.theme.justFamily

@Composable
fun PostScreen(postOID: String, token: String, navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: PostScreenViewModel = viewModel()
    val isLoading by viewModel.isSwipeLoading.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    viewModel.setToken(token)
    viewModel.getPostInfo(snackbarHostState, postOID)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            viewModel.refreshPost(
                snackbarHostState = snackbarHostState,
                postOID = postOID
            )
        }) {
        PostScreenContent(
            viewModel = viewModel,
            navController = navController,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
fun PostScreenContent(
    viewModel: PostScreenViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {

    Scaffold(
        Modifier.fillMaxSize(),
        bottomBar = { CommentBottomBar(viewModel, snackbarHostState) },
        snackbarHost = { SnackbarHost(snackbarHostState) }) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                BackButton(navController = navController)
            }
            item {
                PostComposable(
                    owner = viewModel.username.value,
                    ownerOID = viewModel.ownerOID.value.toString(),
                    title = viewModel.title.value,
                    datePosted = viewModel.postingDate.value,
                    resource = viewModel.resource.value,
                    numberLikes = viewModel.postLikes.value,
                    postOID = viewModel.postOID.value.toString(),
                    navController = navController,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    isOwnedByUser = false,
                    isOnHomePage = false,
                    isSavedDefault = false
                )
            }
            item {
                Text(
                    text = "Comments:",
                    fontFamily = justFamily,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .fillMaxWidth()
                )
            }
            if (viewModel.commentsList.isNotEmpty()) {
                items(viewModel.commentsList) { commentDocument ->
                    CommentComposable(
                        commentOID = (commentDocument["_id"] as ObjectId).toString(),
                        ownerOID = (commentDocument["ownerOID"] as ObjectId).toString(),
                        likeCount = commentDocument["likes"] as Int,
                        content = commentDocument["content"] as String,
                        owner = commentDocument["owner"] as String,
                        navController = navController,
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        isOwnedByUser = false
                    )
                }
            } else {
                item {
                    Text(
                        text = "No comments on this post yet. :(",
                        fontFamily = justFamily,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CommentBottomBar(viewModel: PostScreenViewModel, snackbarHostState: SnackbarHostState) {
    Surface {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = viewModel.commentContent.value,
                onValueChange = { viewModel.commentContent.value = it },
                label = { Text(text = "Add a comment", fontFamily = justFamily) },
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {
                if (viewModel.commentContent.value.isNotEmpty()) {
                    MainScope().launch {
                        val addCommentResponse = addComment(
                            content = viewModel.commentContent.value,
                            postOID = viewModel.postOID.value.toString(),
                            scope = MainScope(),
                            token = viewModel.getToken()
                        )
                        if (addCommentResponse == "true") {
                            snackbarHostState.showSnackbar("Comment added. Refresh the page to see it.")
                            viewModel.commentContent.value = ""
                        } else {
                            snackbarHostState.showSnackbar(
                                message = "Comment added. Refresh the page to see it.",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = "add comment button",
                    Modifier.alpha(if (viewModel.commentContent.value.isNotEmpty()) 1f else 0.3f)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CommentComposable(
    commentOID: String,
    ownerOID: String,
    likeCount: Int,
    content: String,
    owner: String,
    navController: NavController,
    viewModel: PostComposableInterface,
    snackbarHostState: SnackbarHostState,
    isOwnedByUser: Boolean
) {
    val isLiked = rememberSaveable { mutableStateOf(0) }
    val likes = rememberSaveable { mutableStateOf(likeCount) }
    val isDropdownUsed = rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 5.dp, end = 5.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 10.dp, top = 5.dp)
                        .clickable { navController.navigate("profileScreen/$ownerOID/${viewModel.getToken()}") }
                        .wrapContentWidth()) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "account box icon"
                    )
                    Text(
                        text = owner,
                        fontFamily = justFamily,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(start = 3.dp)
                    )
                }
                if (isOwnedByUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            MainScope().launch(Dispatchers.IO) {
                                val removePostResponse = removeComment(
                                    token = viewModel.getToken(),
                                    commentOID = commentOID,
                                    scope = MainScope()
                                )
                                if (removePostResponse == "true") {
                                    snackbarHostState.showSnackbar("Comment deleted successfully. Please refresh.")
                                } else {
                                    snackbarHostState.showSnackbar("Error: $removePostResponse")
                                }
                            }
                        }) {
                        Text(text = "Delete", fontFamily = justFamily)
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "delete comment",
                            modifier = Modifier.padding(end = 5.dp)
                        )
                    }
                }
            }
            Text(
                text = content,
                fontFamily = justFamily,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = {
                    val likesToAdd: Byte
                    if (isLiked.value == 1) {
                        likes.value += -1
                        isLiked.value = 0
                        likesToAdd = -1
                    } else if (isLiked.value == 0) {
                        likes.value += 1
                        isLiked.value = 1
                        likesToAdd = 1
                    } else {
                        likes.value += 2
                        isLiked.value = 1
                        likesToAdd = 2
                    }

                    MainScope().launch {
                        val response = likeCommentAction(
                            value = likesToAdd.toInt(),
                            commentOID = commentOID,
                            token = viewModel.getToken(),
                            scope = MainScope()
                        )
                        if (response != "true") {
                            snackbarHostState.showSnackbar("Error: $response")
                        }
                    }
                }) {
                    AnimatedContent(targetState = isLiked.value, label = "") {
                        Icon(
                            imageVector = if (it == 1) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "like button",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                AnimatedContent(targetState = likes.value, label = "") {
                    Text(
                        text = "$it",
                        fontFamily = justFamily,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                IconButton(onClick = {
                    val likesToAdd: Byte
                    if (isLiked.value == 1) {
                        likes.value += -2
                        isLiked.value = -1
                        likesToAdd = -2
                    } else if (isLiked.value == 0) {
                        likes.value += -1
                        isLiked.value = -1
                        likesToAdd = -1
                    } else {
                        likes.value += 1
                        isLiked.value = 0
                        likesToAdd = 1
                    }

                    MainScope().launch {
                        val response = likeCommentAction(
                            value = likesToAdd.toInt(),
                            commentOID = commentOID,
                            token = viewModel.getToken(),
                            scope = MainScope()
                        )
                        if (response != "true") {
                            snackbarHostState.showSnackbar("Error: $response")
                        }
                    }
                }) {
                    AnimatedContent(targetState = isLiked.value, label = "") {
                        Icon(
                            imageVector = if (it == -1) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                            contentDescription = "dislike button",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CommentComposablePreview() {
    CommentComposable(
        commentOID = "aaa",
        ownerOID = "aaaa",
        likeCount = 123,
        content = "I like caca",
        owner = "Gigel",
        navController = rememberNavController(),
        viewModel = PostScreenViewModel(),
        snackbarHostState = SnackbarHostState(),
        isOwnedByUser = false
    )
}