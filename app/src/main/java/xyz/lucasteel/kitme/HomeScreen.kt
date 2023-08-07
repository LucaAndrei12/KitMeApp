package xyz.lucasteel.kitme

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.bson.Document
import xyz.lucasteel.kitme.logic.getToken
import xyz.lucasteel.kitme.logic.likePostAction
import xyz.lucasteel.kitme.logic.removePost
import xyz.lucasteel.kitme.logic.savePostAction
import xyz.lucasteel.kitme.ui.theme.justFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val homeScreenViewModel = HomeScreenViewModel()
    val homeSnackbarHostState = remember { SnackbarHostState() }
    homeScreenViewModel.setToken(getToken(LocalContext.current))
    homeScreenViewModel.getFeed(homeSnackbarHostState, navController)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = { BottomNavigation(navController = navController) },
        topBar = { HomeScreenAppBar(scrollBehavior, navController) },
        floatingActionButton = { HomeScreenActionButton(navController, homeScreenViewModel) }) {
        Surface(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            HomeScreenContent(
                navController = navController,
                viewModel = homeScreenViewModel,
                snackbarHostState = homeSnackbarHostState
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    navController: NavController,
    viewModel: HomeScreenViewModel,
    snackbarHostState: SnackbarHostState
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp),
        content = {
            items(items = viewModel.postsList.value,
                key = {println(it); Document.parse((Document.parse("$it}")["_id"] as String))["\$oid"] as String}
            ) {
                val postDocument = Document.parse(it)
                PostComposable(
                    owner = postDocument["owner"] as String,
                    ownerOID = postDocument["ownerOID"] as String,
                    title = postDocument["title"] as String,
                    datePosted = postDocument["postingDate"] as String,
                    resource = postDocument["resource"] as String,
                    numberLikes = postDocument["likes"] as Long,
                    postOID = postDocument["_id"] as String,
                    navController = navController,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    isOwnedByUser = false
                )
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenAppBar(scrollBehavior: TopAppBarScrollBehavior, navController: NavController) {
    TopAppBar(scrollBehavior = scrollBehavior, title = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "KitMe",
                    fontFamily = justFamily,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 5.dp)
                )
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "paw icon",
                    modifier = Modifier.padding(3.dp)
                )
            }
            Row(
                modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate("infoScreen") }) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "information icon")
                }
            }
        }
    }
    )


}

@Composable
fun HomeScreenActionButton(navController: NavController, viewModel: HomeScreenViewModel) {
    FloatingActionButton(onClick = { navController.navigate("addPostScreen/${viewModel.getToken()}") }) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "add post button")
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PostComposable(
    owner: String,
    ownerOID: String,
    title: String,
    datePosted: String,
    resource: String,
    numberLikes: Long,
    postOID: String,
    navController: NavController,
    viewModel: HomeScreenViewModel,
    snackbarHostState: SnackbarHostState,
    isOwnedByUser: Boolean
) {
    val circleColor = MaterialTheme.colorScheme.secondary
    val isLiked = remember { mutableStateOf(0) }
    val isSaved = remember { mutableStateOf(false) }
    val isMenuExpanded = remember { mutableStateOf(false) }
    var likes = remember { mutableStateOf(numberLikes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { navController.navigate("postScreen/$postOID/${viewModel.getToken()}") }
            .padding(start = 5.dp, end = 5.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 10.dp, top = 5.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "user profile picture"
                    )
                    Text(
                        text = owner,
                        fontFamily = justFamily,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(end = 3.dp)
                            .clickable {
                                navController.navigate("profileScreen/$ownerOID/${viewModel.getToken()}")
                            }
                    )
                    Canvas(modifier = Modifier.size(7.dp), onDraw = {
                        drawCircle(color = circleColor)
                    })
                    Text(
                        text = datePosted,
                        fontFamily = justFamily,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOwnedByUser) {
                        if (isMenuExpanded.value) {
                            DropdownMenu(
                                expanded = isMenuExpanded.value,
                                onDismissRequest = { isMenuExpanded.value = false }) {
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        MainScope().launch(Dispatchers.IO) {
                                            val removePostResponse = removePost(
                                                token = viewModel.getToken(),
                                                postOID = postOID,
                                                scope = MainScope()
                                            )
                                            if (removePostResponse.equals("true")) {
                                                snackbarHostState.showSnackbar("Post deleted successfully.")
                                            } else {
                                                snackbarHostState.showSnackbar("Error: $removePostResponse")
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "delete button"
                                        )
                                    })
                            }
                        }
                        IconButton(
                            onClick = { isMenuExpanded.value = !isMenuExpanded.value }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "dropdown menu"
                            )
                        }
                    }
                }
            }
            Text(
                text = title,
                fontFamily = justFamily,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 10.dp, end = 5.dp, top = 5.dp, bottom = 5.dp)
            )
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(resource)
                    .crossfade(true)
                    .build(),
                loading = { CircularProgressIndicator() },
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .align(Alignment.CenterHorizontally)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        MainScope().launch {
                            val response = likePostAction(
                                value = if (isLiked.value == 1) false else true,
                                postOID = postOID,
                                token = viewModel.getToken(),
                                scope = MainScope()
                            )
                            if (response != "true") {
                                snackbarHostState.showSnackbar("Error: $response")
                            }
                        }
                        likes.value += if (isLiked.value == 1) -1 else 1
                        isLiked.value = if (isLiked.value == 1) 0 else 1
                    }) {
                        AnimatedContent(targetState = isLiked) {
                            if (it.value == 1) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbUp,
                                    contentDescription = "like button",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.ThumbUp,
                                    contentDescription = "like button",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    AnimatedContent(targetState = likes) {
                        Text(
                            text = "${likes.value}",
                            fontFamily = justFamily,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    IconButton(onClick = {
                        MainScope().launch {
                            val response = likePostAction(
                                value = if (isLiked.value == -1) true else false,
                                postOID = postOID,
                                token = viewModel.getToken(),
                                scope = MainScope()
                            )
                            if (response != "true") {
                                snackbarHostState.showSnackbar("Error: $response")
                            }
                        }
                        likes.value += if (isLiked.value == -1) 1 else -1
                        isLiked.value = if (isLiked.value == -1) 0 else -1
                    }) {
                        AnimatedContent(targetState = isLiked) {
                            if (it.value == -1) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbDown,
                                    contentDescription = "like button",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.ThumbDown,
                                    contentDescription = "like button",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = {
                    MainScope().launch(Dispatchers.IO) {
                        val response = savePostAction(
                            scope = MainScope(),
                            postOID = postOID,
                            token = viewModel.getToken(),
                            save = !isSaved.value
                        )
                        if (response != "true") {
                            snackbarHostState.showSnackbar("Error: $response")
                        }
                    }
                    isSaved.value = !isSaved.value
                }, modifier = Modifier.padding(3.dp)) {
                    AnimatedContent(targetState = isSaved) {
                        if (it.value) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = "saved post"
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = "unsaved post"
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreenPreview() {
    PostComposable(
        owner = "Gigel",
        ownerOID = "aaaa",
        title = "Meet my new boy, adrian",
        datePosted = "15 feb 2023",
        resource = "https://images.unsplash.com/photo-1595433707802-6b2626ef1c91?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxleHBsb3JlLWZlZWR8Mnx8fGVufDB8fHx8fA%3D%3D&w=1000&q=80",
        numberLikes = 10,
        postOID = "aaa",
        navController = rememberNavController(),
        viewModel = HomeScreenViewModel(),
        snackbarHostState = remember { SnackbarHostState() },
        isOwnedByUser = false
    )
}