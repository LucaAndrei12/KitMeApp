package xyz.lucasteel.kitme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xyz.lucasteel.kitme.logic.getToken
import xyz.lucasteel.kitme.ui.theme.KitMeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KitMeAppTheme {
                val context = LocalContext.current
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = if (getToken(context = context).equals("")) "loginScreen" else "homeScreen"
                ) {

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

                    composable(route = "signUpScreen") {
                        SignUpScreen(navController = navController)
                    }

                    composable(route = "postScreen/{postOID}/{token}") {
                        PostScreen(
                            postOID = it.arguments?.getString("postOID")!!,
                            token = it.arguments?.getString("token")!!,
                            navController = navController
                        )
                    }

                    composable(route = "profileScreen/{userOID}/{token}") {
                        ProfileScreen(
                            userOID = it.arguments?.getString("userOID")!!,
                            token = it.arguments?.getString("token")!!,
                            navController = navController
                        )
                    }

                    composable(route = "savedScreen") {
                        SavedScreen(
                            token = getToken(LocalContext.current),
                            navController = navController
                        )
                    }

                    composable(route = "searchScreen") {
                        SearchScreen(
                            token = getToken(LocalContext.current),
                            navController = navController
                        )
                    }

                    composable(route = "userScreen") {
                        UserScreen(
                            navController = navController
                        )
                    }

                    composable(route = "verifyOTPScreen/{unsavedToken}") {
                        VerifyOTPScreen(
                            unsavedToken = it.arguments?.getString("unsavedToken")!!,
                            navController = navController
                        )
                    }

                    composable(route = "forgotPasswordScreen") {
                        ForgotPasswordScreen(
                            navController = navController
                        )
                    }

                    composable(route = "forgotUsernameScreen") {
                        ForgotUsernameScreen(
                            navController = navController
                        )
                    }

                    composable(route = "infoScreen") {
                        InfoScreen(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

