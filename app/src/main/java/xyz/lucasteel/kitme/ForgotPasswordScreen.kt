package xyz.lucasteel.kitme

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.lucasteel.kitme.logic.forgotPassword
import xyz.lucasteel.kitme.logic.updateOTP
import xyz.lucasteel.kitme.logic.updateOTPUsername
import xyz.lucasteel.kitme.logic.verifyOTP
import xyz.lucasteel.kitme.logic.verifyOTPUsername
import xyz.lucasteel.kitme.ui.theme.justFamily

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val viewModel = ForgotPasswordScreenViewModel()
    val snackbarHostStateForgotPassword = remember { SnackbarHostState() }
    ForgotPasswordScreenContent(
        navController = navController,
        viewModel = viewModel,
        snackbarHostState = snackbarHostStateForgotPassword
    )
}

@Composable
fun ForgotPasswordScreenContent(
    navController: NavController,
    viewModel: ForgotPasswordScreenViewModel,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BackButton(navController = navController)
            ForgotPasswordTitle()
            Column(verticalArrangement = Arrangement.Center) {
                UsernameTextFieldResetPassword(viewModel = viewModel)
                OTPCodeField(viewModel = viewModel, snackbarHostState = snackbarHostState)
                NewPasswordField(viewModel = viewModel)
                ConfirmNewPasswordField(viewModel = viewModel)
            }
            ResetPasswordButton(viewModel = viewModel, snackbarHostState = snackbarHostState, navController = navController)
            Image(
                painter = painterResource(R.drawable.signup_kitme_cropped),
                contentDescription = "kitten photos",
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun UsernameTextFieldResetPassword(viewModel: ForgotPasswordScreenViewModel) {
    OutlinedTextField(
        value = viewModel.username.value,
        onValueChange = { viewModel.username.value = it },
        label = { Text(text = "Username", fontFamily = justFamily) })
}

@Composable
fun ForgotPasswordTitle() {
    Text(
        text = "Reset your password",
        fontFamily = justFamily,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun OTPCodeField(viewModel: ForgotPasswordScreenViewModel, snackbarHostState: SnackbarHostState) {
    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 5.dp)) {
        OutlinedTextField(
            modifier = Modifier.padding(start = 20.dp),
            value = viewModel.otpValue.value,
            onValueChange = { if (it.length <= 4) viewModel.otpValue.value = it },
            label = { Text(text = "One-time password", fontFamily = justFamily) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        if (viewModel.isResendAvailable.value) {
            Text(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .alpha(0.5f),
                text = "Send OTP!(${viewModel.millisecondsLeftUntilAvailable.value / 1000}s)",
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.tertiary
            )
        } else {
            Text(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .clickable {
                        if (viewModel.username.value != "") {
                            MainScope().launch(Dispatchers.IO) {
                                val sendOTPResponse = updateOTPUsername(
                                    scope = MainScope(),
                                    username = viewModel.username.value
                                )
                                if (sendOTPResponse == "true") {
                                    snackbarHostState.showSnackbar("If an account corresponds to the given username, we've sent it an email.")
                                    viewModel.startSecondsCountDown()
                                } else {
                                    snackbarHostState.showSnackbar("An error occurred. Please try again.")
                                }
                            }
                        } else {
                            MainScope().async { snackbarHostState.showSnackbar("Please provide a valid username.") }
                        }
                    },
                text = "Send OTP!",
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun NewPasswordField(viewModel: ForgotPasswordScreenViewModel) {
    OutlinedTextField(
        value = viewModel.newPassword.value,
        onValueChange = { viewModel.newPassword.value = it },
        label = { Text(text = "New password", fontFamily = justFamily) }
        , modifier = Modifier.padding(top = 5.dp))
}

@Composable
fun ConfirmNewPasswordField(viewModel: ForgotPasswordScreenViewModel) {
    OutlinedTextField(
        value = viewModel.confirmNewPassword.value,
        onValueChange = { viewModel.confirmNewPassword.value = it },
        label = { Text(text = "Confirm new password", fontFamily = justFamily) }
        , modifier = Modifier.padding(top = 5.dp))
}

@Composable
fun ResetPasswordButton(
    viewModel: ForgotPasswordScreenViewModel,
    snackbarHostState: SnackbarHostState,
    navController: NavController
) {
    Button(onClick = {
        MainScope().async(Dispatchers.IO) {
            if (viewModel.arePasswordsMatching()) {
                if (viewModel.otpValue.value.length == 4) {
                    val isOTPValid = verifyOTPUsername(
                        scope = MainScope(),
                        username = viewModel.username.value,
                        otp = Integer.parseInt(viewModel.otpValue.value)
                    )
                    if (isOTPValid) {
                        val isForgotPasswordSuccessful = forgotPassword(
                            scope = MainScope(),
                            username = viewModel.username.value,
                            newPassword = viewModel.confirmNewPassword.value,
                            otp = Integer.parseInt(viewModel.otpValue.value)
                        )
                        if (isForgotPasswordSuccessful) {
                            snackbarHostState.showSnackbar("Password resetting successful. Redirecting you...")
                            navController.navigate("loginScreen")
                        } else {
                            snackbarHostState.showSnackbar("An error occurred. Please try again.")
                        }
                    } else {
                        snackbarHostState.showSnackbar("OTP is incorrect or an error occurred.")
                    }
                } else {
                    snackbarHostState.showSnackbar("Please provide a valid OTP.")
                }
            } else {
                snackbarHostState.showSnackbar("The passwords are not matching.")
            }
        }
    }
    ) {
        Text(text = "Reset password!", fontFamily = justFamily)
    }
}