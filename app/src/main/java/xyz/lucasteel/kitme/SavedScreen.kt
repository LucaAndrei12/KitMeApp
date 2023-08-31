package xyz.lucasteel.kitme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import xyz.lucasteel.kitme.logic.getUser
import xyz.lucasteel.kitme.ui.theme.justFamily

@Composable
fun SavedScreen(token:String, navController: NavController){

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreenTopBar(){
    TopAppBar(title = {
        Text(
            text = "Your saved posts",
            fontFamily = justFamily,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 5.dp)
        )
        Icon(
            imageVector = Icons.Filled.Bookmark,
            contentDescription = "bookmark",
            modifier = Modifier.padding(3.dp)
        )
    })
}

@Composable
fun SavedScreenContent(navController: NavController, token: String, viewModel: SavedScreenViewModel){
    Scaffold(topBar = { SavedScreenTopBar() }, bottomBar = { BottomNavigation(navController = navController) }) {
        Surface(Modifier.fillMaxSize().padding(it)) {

        }
    }
}