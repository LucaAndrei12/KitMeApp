package xyz.lucasteel.kitme

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import xyz.lucasteel.kitme.logic.saveTokenToFile
import xyz.lucasteel.kitme.logic.updateOTP
import xyz.lucasteel.kitme.logic.verifyOTP
import xyz.lucasteel.kitme.ui.theme.justFamily

private val viewModel: VerifyOTPScreenViewModel = VerifyOTPScreenViewModel()

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VerifyOTPScreen(unsavedToken: String, navController: NavController) {
    val verifyOTPSnackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = {
        SnackbarHost(hostState = verifyOTPSnackbarHostState)
    }) {
        VerifyOTPScreenContent(
            navController = navController,
            unsavedToken = unsavedToken,
            verifyOTPSnackbarHostState
        )
    }
}

@Composable
fun VerifyOTPScreenContent(
    navController: NavController,
    unsavedToken: String,
    snackbarHost: SnackbarHostState
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BackButton(navController = navController)
        Text(
            text = "Verify your email",
            fontFamily = justFamily,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Column(
            modifier = Modifier.wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Please check your inbox(and spam too!) for the email that we've sent you. P.S. It expires in 5 minutes.",
                    fontFamily = justFamily,
                    style = MaterialTheme.typography.bodyLarge,
                    minLines = 2,
                    modifier = Modifier.padding(top = 5.dp, end = 20.dp, start = 20.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            ) {
                OTPTextField()
                ResendText(unsavedToken = unsavedToken)
            }
            if (viewModel.isLoading.value) {
                CircularProgressIndicator()
            } else {
                VerifyButton(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    unsavedToken = unsavedToken
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.kitme_verify_email),
            contentDescription = "an image with an inbox",
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun OTPTextField() {
    OutlinedTextField(
        modifier = Modifier.padding(start = 20.dp),
        value = viewModel.otpText.value,
        onValueChange = { if (it.length <= 4) viewModel.otpText.value = it },
        label = { Text(text = "One-time password", fontFamily = justFamily) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun BackButton(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        IconButton(onClick = { navController.navigateUp() }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "go back button")
        }
    }
}

@Composable
fun VerifyButton(
    navController: NavController,
    snackbarHost: SnackbarHostState,
    unsavedToken: String
) {
    val context = LocalContext.current
    Button(onClick = {
        MainScope().launch {
            try {
                viewModel.isLoading.value = true
                val isOTPValid = verifyOTP(
                    MainScope(),
                    unsavedToken,
                    Integer.parseInt(viewModel.otpText.value)
                )
                println(isOTPValid)
                if (isOTPValid) {
                    snackbarHost.showSnackbar(
                        "OTP is correct. Redirecting you...",
                        duration = SnackbarDuration.Short
                    )
                    viewModel.isLoading.value = false
                    saveTokenToFile(context = context, token = unsavedToken)
                    navController.navigate("homeScreen")
                } else {
                    snackbarHost.showSnackbar(
                        "OTP is incorrect or an error occurred.",
                        duration = SnackbarDuration.Long
                    )
                    viewModel.isLoading.value = false
                }
            } catch (e: Exception) {
                snackbarHost.showSnackbar(
                    "Error: ${e.message!!}",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }, modifier = Modifier.padding(top = 5.dp)) {
        Text(text = "Verify!", fontFamily = justFamily)
    }
}

@Composable
fun ResendText(unsavedToken: String) {
    Text(
        text = "Resend OTP" + if (!viewModel.isResendAvailable.value) " (${viewModel.millisecondsLeftUntilAvailable.value / 1000}s)" else "",
        color = MaterialTheme.colorScheme.tertiary,
        textDecoration = TextDecoration.Underline,
        fontFamily = justFamily,
        modifier = Modifier
            .padding(start = 5.dp)
            .alpha(if (viewModel.isResendAvailable.value) 1f else 0.5f)
            .clickable {
                if (viewModel.isResendAvailable.value) {
                    MainScope().launch {
                        updateOTP(MainScope(), unsavedToken)
                    }
                    viewModel.startStateResendCountDown()
                    viewModel.startSecondsCountDown()
                }
            }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun VerifyOTPScreenPreview() {
    val verifyOTPSnackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = {
        SnackbarHost(hostState = verifyOTPSnackbarHostState)
    }) {
        VerifyOTPScreenContent(
            navController = rememberNavController(),
            unsavedToken = "Aaa",
            remember { SnackbarHostState() }
        )
    }
}