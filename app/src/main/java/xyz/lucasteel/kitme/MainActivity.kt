package xyz.lucasteel.kitme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.MainScope
import xyz.lucasteel.kitme.logic.verifyOTP
import xyz.lucasteel.kitme.ui.theme.KitMeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KitMeAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "homeScreen") {

                    composable(route = "homeScreen") {
                        HomeScreen(navController = navController)
                    }

                    composable(route = "addPostScreen/{token}") {
                        AddPostScreen(
                            token = it.arguments?.getString("token")!!,
                            navController = navController
                        )
                    }

                    composable(route = "loginScreen") {
                        LogInScreen(navController = navController)
                    }

                    composable(route = "signUpScreen"){
                        SignUpScreen(navController = navController)
                    }

                    composable(route = "postScreen/{postOID}/{token}"){
                        PostScreen(
                            postOID = it.arguments?.getString("postOID")!!,
                            token = it.arguments?.getString("token")!!,
                            navController = navController
                        )
                    }

                    composable(route = "profileScreen/{userOID}/{token}"){
                        ProfileScreen(
                            userOID = it.arguments?.getString("userOID")!!,
                            token = it.arguments?.getString("token")!!,
                            navController = navController
                        )
                    }

                    composable(route = "savedScreen/{token}"){
                        SavedScreen(
                            token = it.arguments?.getString("token")!!,
                            navController = navController
                        )
                    }

                    composable(route = "searchScreen/{token}"){
                        SearchScreen(
                            token = it.arguments?.getString("token")!!,
                            navController = navController
                        )
                    }

                    composable(route = "userScreen/{token}"){
                        UserScreen(
                            token = it.arguments?.getString("token")!!,
                            navController = navController
                        )
                    }

                    composable(route = "verifyOTPScreen/{unsavedToken}"){
                        VerifyOTPScreen(
                            unsavedToken = it.arguments?.getString("unsavedToken")!!,
                            navController = navController
                        )
                    }

                    composable(route = "forgotPasswordScreen}"){
                        ForgotPasswordScreen(
                            navController = navController
                        )
                    }

                    composable(route = "forgotUsernameScreen}"){
                        ForgotPasswordScreen(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

