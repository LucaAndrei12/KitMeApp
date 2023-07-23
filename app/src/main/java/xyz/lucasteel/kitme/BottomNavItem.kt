package xyz.lucasteel.kitme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(name = "Home", route = "homeScreen", icon = Icons.Filled.Home),
    BottomNavItem(name = "Search", route = "searchScreen", icon = Icons.Filled.Search),
    BottomNavItem(name = "Saved", route = "savedScreen", icon = Icons.Filled.Bookmarks),
    BottomNavItem(name = "Profile", route = "userScreen", icon = Icons.Filled.Person)
)

@Composable
fun BottomNavigation(navController: NavController) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    NavigationBar(
        tonalElevation = 3.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route

            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigate(item.route) },
                label = {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = "${item.name} Icon"
                    )
                })
        }
    }
}

@Preview
@Composable
fun BottomNavPreview(){
    BottomNavigation(navController = rememberNavController())
}
