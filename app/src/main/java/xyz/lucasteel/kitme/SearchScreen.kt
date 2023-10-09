package xyz.lucasteel.kitme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import org.bson.types.ObjectId
import xyz.lucasteel.kitme.logic.getToken
import xyz.lucasteel.kitme.ui.theme.justFamily

@Composable
fun SearchScreen(token: String, navController: NavController) {
    val viewModel: SearchScreenViewModel = viewModel()
    viewModel.token.value = getToken(LocalContext.current)
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, bottomBar = {
        BottomNavigation(
            navController = navController
        )
    }) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchBar(viewModel = viewModel, snackbarHostState = snackbarHostState)
                PostOrUserTabs(viewModel = viewModel)
                SearchResultView(
                    viewModel = viewModel,
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun SearchBar(viewModel: SearchScreenViewModel, snackbarHostState: SnackbarHostState) {
    TextField(
        value = viewModel.textQuery.value,
        onValueChange = {
            viewModel.textQuery.value = it; viewModel.updateWithSearch(
            snackbarHostState = snackbarHostState,
            isPostSearch = viewModel.isPostSearch.value,
            query = viewModel.textQuery.value
        )
        }, label = { Text(text = "Search KitMe", fontFamily = justFamily) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "search icon"
            )
        },
        trailingIcon = {
            if (viewModel.textQuery.value.isNotEmpty()) {
                IconButton(onClick = { viewModel.textQuery.value = "" }) {
                    Icon(
                        imageVector = Icons.Outlined.Cancel,
                        contentDescription = "delete all text from search"
                    )
                }
            }
        }, modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 5.dp)
    )
}

@Composable
fun PostOrUserTabs(viewModel: SearchScreenViewModel) {
    TabRow(
        selectedTabIndex = if (viewModel.isPostSearch.value) 1 else 0,
        modifier = Modifier.fillMaxWidth()
    ) {
        Tab(
            selected = !viewModel.isPostSearch.value,
            onClick = { viewModel.isPostSearch.value = false }) {
            Text(
                text = "Users",
                fontFamily = justFamily,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
            )
        }
        Tab(
            selected = viewModel.isPostSearch.value,
            onClick = { viewModel.isPostSearch.value = true }) {
            Text(
                text = "Posts",
                fontFamily = justFamily,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
            )
        }
    }
}

@Composable
fun SearchResultView(
    viewModel: SearchScreenViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    if (viewModel.isLoading.value) {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    } else if (viewModel.textQuery.value.isEmpty()) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = "Come on, search something :)",
                fontFamily = justFamily,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 5.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (viewModel.isPostSearch.value && viewModel.foundPostsList.value.isNotEmpty() || !viewModel.isPostSearch.value && viewModel.foundUsersList.value.isNotEmpty()) {
                if (viewModel.isPostSearch.value) {
                    items(items = viewModel.foundPostsList.value) {
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
                            isOwnedByUser = false,
                            isOnHomePage = false,
                            isSavedDefault = false
                        )
                    }
                } else {
                    items(items = viewModel.foundUsersList.value) {
                        UserCard(
                            navController = navController,
                            username = it["username"]!! as String,
                            dateJoined = it["dateJoined"]!! as String,
                            profilePicture = it["profilePicture"]!! as String,
                            userOID = (it["_id"]!! as ObjectId).toString(),
                            sessionToken = viewModel.getToken()
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "Nothing found...",
                        fontFamily = justFamily,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun UserCard(
    navController: NavController,
    username: String,
    dateJoined: String,
    profilePicture: String,
    userOID: String,
    sessionToken: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 5.dp, end = 5.dp)
            .clickable { navController.navigate("profileScreen/$userOID/$sessionToken") },
        elevation = CardDefaults.cardElevation()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubcomposeAsyncImage(
                model = profilePicture,
                contentDescription = "user's profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .padding(10.dp)
                    .clip(CircleShape)
            )
            Column {
                Text(
                    text = username,
                    fontFamily = justFamily,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Joined KitMe $dateJoined",
                    fontFamily = justFamily,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}