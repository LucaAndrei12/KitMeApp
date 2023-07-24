package xyz.lucasteel.kitme

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import xyz.lucasteel.kitme.ui.theme.justFamily

//FIND TO TOKEN IN FILE, IF NOT FOUND NAVIGATE TO LOGIN SCREEN
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigation(navController = navController) },
        content = { HomeScreenContent() })
}

@Composable
fun HomeScreenContent() {
    Surface(modifier = Modifier.fillMaxSize()){
    Column(
        modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = "This cool", style = MaterialTheme.typography.headlineMedium, fontFamily = justFamily)
        Image(
            painter = painterResource(id = R.drawable.login_kitme_cropped),
            contentDescription = "kittens",
            Modifier.height(100.dp).fillMaxWidth()
        )
    }
}
}

@Preview
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreenPreview() {
    Scaffold(
        bottomBar = { BottomNavigation(rememberNavController()) },
        content = { HomeScreenContent() })
}