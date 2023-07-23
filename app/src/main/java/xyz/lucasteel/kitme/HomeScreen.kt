package xyz.lucasteel.kitme

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

//FIND TO TOKEN IN FILE, IF NOT FOUND NAVIGATE TO LOGIN SCREEN
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController){
    Scaffold(bottomBar = { BottomNavigation(navController = navController) }, content = {Text(text = "aaa")})
}