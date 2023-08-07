package xyz.lucasteel.kitme

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import xyz.lucasteel.kitme.logic.forgotUsername
import xyz.lucasteel.kitme.ui.theme.justFamily

private val viewModel = ForgotUsernameScreenViewModel()

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ForgotUsernameScreen(navController: NavController) {
    val forgotUsernameSnackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(hostState = forgotUsernameSnackbarHostState) }) {
        ForgotUsernameScreenContent(
            navController = navController,
            snackbarHost = forgotUsernameSnackbarHostState,
        )
    }
}

@Composable
fun ForgotUsernameScreenContent(
    navController: NavController,
    snackbarHost: SnackbarHostState
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(navController = navController)
        Text(
            text = "Forgot your username?",
            fontFamily = justFamily,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No problem, we'll make sure one of our katz will deliver it straight into your inbox(sometimes they bump into Spam). Just provide your account's email address.",
                fontFamily = justFamily,
                style = MaterialTheme.typography.bodyLarge,
                minLines = 2,
                modifier = Modifier.padding(top = 5.dp, end = 20.dp, start = 20.dp, bottom = 5.dp)
            )
            EmailTextField()
            if (viewModel.isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 5.dp))
            } else {
                ForgotUsernameButton(
                    snackbarHost = snackbarHost,
                    navController = navController
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.kitme_verify_email),
            contentDescription = "a photo with an inbox"
        )
    }
}

@Composable
fun EmailTextField() {
    OutlinedTextField(
        value = viewModel.emailText.value,
        onValueChange = {
            viewModel.emailText.value = it
            viewModel.checkEmail()
        },
        label = { Text(text = "Account email", fontFamily = justFamily) },
        singleLine = true,
        isError = viewModel.isEmailError.value,
        supportingText = {
            if (viewModel.isEmailError.value) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = viewModel.emailTip.value,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }, trailingIcon = {
            if (viewModel.isEmailError.value)
                Icon(Icons.Default.Error, "error", tint = MaterialTheme.colorScheme.error)
        },
        keyboardActions = KeyboardActions { viewModel.checkEmail() }
    )
}

@Composable
fun ForgotUsernameButton(
    snackbarHost: SnackbarHostState,
    navController: NavController
) {
    Button(onClick = {
            MainScope().launch {
                if (!viewModel.isEmailError.value) {
                viewModel.isLoading.value = true
                try {
                    val forgotUsernameResponse =
                        forgotUsername(email = viewModel.emailText.value, scope = MainScope())
                    if (forgotUsernameResponse.equals("true")) {
                        viewModel.isLoading.value = false
                        snackbarHost.showSnackbar(
                            message = "If there is an account corresponding to the address provided, we'll send your username! <3",
                            duration = SnackbarDuration.Short
                        )
                        navController.navigate("loginScreen")
                    } else {
                        viewModel.isLoading.value = false
                        snackbarHost.showSnackbar("Error: $forgotUsernameResponse")
                    }
                } catch (e: Exception) {
                    viewModel.isLoading.value = false
                    snackbarHost.showSnackbar("Error: ${e.message}")
                } } else {
                    MainScope().launch {
                        snackbarHost.showSnackbar(
                            "Please provide a valid email address.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
    }, modifier = Modifier.padding(top = 5.dp)) {
        Text(text = "Email my username!", fontFamily = justFamily)
    }
}