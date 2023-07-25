package xyz.lucasteel.kitme

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.bson.Document
import xyz.lucasteel.kitme.logic.signUp
import xyz.lucasteel.kitme.ui.theme.justFamily

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SignUpScreen(navController: NavController) {
    val signUpViewModel = SignUpScreenViewModel()
    val signUpSnackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = signUpSnackbarHostState)
        }
    ) {
        SignUpScreenContent(signUpViewModel, navController, signUpSnackbarHostState)
    }
}

@Composable
fun SignUpScreenContent(
    viewModel: SignUpScreenViewModel,
    navController: NavController,
    snackbarHost: SnackbarHostState
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WelcomeKitmeTexts()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Create an account:",
                    modifier = Modifier
                        .padding(bottom = 5.dp, start = 5.dp)
                        .align(Alignment.Start),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = justFamily
                )
                UsernameTextField(viewModel = viewModel)
                EmailTextField(viewModel = viewModel)
                PasswordTextField(viewModel = viewModel)
                ConfirmPasswordTextField(viewModel = viewModel)
                if(viewModel.isLoading.value){
                    CircularProgressIndicator(modifier = Modifier.padding(top = 10.dp))
                } else {
                    SignUpButton(
                        viewModel = viewModel,
                        navController = navController,
                        context = LocalContext.current,
                        snackbarHost = snackbarHost
                    )
                }
                AlreadyUserText(navController = navController)
            }
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
fun EmailTextField(viewModel: SignUpScreenViewModel) {
    OutlinedTextField(
        value = viewModel.emailText.value,
        onValueChange = {
            viewModel.emailText.value = it
            viewModel.checkEmail()
        },
        label = { Text(text = "Email", fontFamily = justFamily) },
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
fun UsernameTextField(viewModel: SignUpScreenViewModel) {
    OutlinedTextField(
        value = viewModel.usernameText.value,
        onValueChange = {
            viewModel.usernameText.value = it
            viewModel.checkUsername()
        },
        label = { Text(text = "Username", fontFamily = justFamily) },
        singleLine = true,
        isError = viewModel.isUsernameError.value,
        supportingText = {
            if (viewModel.isUsernameError.value) {
                Text(
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    text = viewModel.usernameTip.value,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }, trailingIcon = {
            if (viewModel.isUsernameError.value)
                Icon(Icons.Default.Error, "error", tint = MaterialTheme.colorScheme.error)
        },
        keyboardActions = KeyboardActions { viewModel.checkUsername() }
    )
}

@Composable
fun PasswordTextField(viewModel: SignUpScreenViewModel) {
    OutlinedTextField(
        value = viewModel.passwordText.value,
        onValueChange = { viewModel.passwordText.value = it; viewModel.checkPasswords() },
        label = { Text(text = "Password", fontFamily = justFamily) },
        singleLine = true,
        visualTransformation = if (viewModel.isPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (viewModel.isPasswordVisible.value)
                Icons.Filled.Visibility
            else Icons.Filled.VisibilityOff

            // Please provide localized description for accessibility services
            val description =
                if (viewModel.isPasswordVisible.value) "Hide password" else "Show password"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 3.dp)
            ) {
                IconButton(onClick = {
                    viewModel.isPasswordVisible.value = !viewModel.isPasswordVisible.value
                }) {
                    Icon(imageVector = image, description)
                }
                if (viewModel.isPasswordError.value) {
                    Icon(imageVector = Icons.Default.Error, contentDescription = "Error icon")
                }
            }
        }, isError = viewModel.isPasswordError.value,
        supportingText = {
            if (viewModel.isPasswordError.value) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    text = viewModel.passwordTip.value,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun ConfirmPasswordTextField(viewModel: SignUpScreenViewModel) {
    OutlinedTextField(
        value = viewModel.confirmPasswordText.value,
        onValueChange = { viewModel.confirmPasswordText.value = it; viewModel.checkPasswords() },
        label = { Text(text = "Confirm password", fontFamily = justFamily) },
        singleLine = true,
        visualTransformation = if (viewModel.isConfirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = {
                viewModel.isConfirmPasswordVisible.value = !viewModel.isConfirmPasswordVisible.value
            }) {
                Icon(
                    imageVector = if (viewModel.isConfirmPasswordVisible.value) Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff, "visibilityIcons"
                )
            }
        }
    )
}

@Composable
fun AlreadyUserText(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text =
            buildAnnotatedString {
                append("Already a user? ")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.tertiary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Log in!")
                }
            },
            modifier = Modifier
                .padding(bottom = 5.dp, top = 15.dp)
                .clickable { navController.navigate("loginScreen") },
            fontFamily = justFamily,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SignUpButton(
    viewModel: SignUpScreenViewModel,
    navController: NavController,
    context: Context,
    snackbarHost: SnackbarHostState
) {
    Button(onClick = {
        if (viewModel.areAllStatesGood()) {
            SafetyNet.getClient(context).verifyWithRecaptcha(CAPTCHA_SITE_KEY)
                .addOnSuccessListener(OnSuccessListener { response ->
                    // Indicates communication with reCAPTCHA service was
                    // successful.
                    val captchaToken = response.tokenResult
                    if (response.tokenResult?.isNotEmpty() == true) {
                        MainScope().launch {
                            viewModel.isLoading.value = true
                            val signUpResponse = signUp(
                                scope = MainScope(),
                                email = viewModel.emailText.value,
                                username = viewModel.usernameText.value,
                                password = viewModel.passwordText.value,
                                captcha = captchaToken!!
                            )
                            if (signUpResponse.contains("token")) {
                                val token = Document.parse(signUpResponse).get("token") as String
                                snackbarHost.showSnackbar(
                                    message = "Sign up successful. Redirecting you...",
                                    duration = SnackbarDuration.Short
                                )
                                navController.navigate("verifyOTPScreen/$token")
                            } else {
                                viewModel.isLoading.value = false
                                snackbarHost.showSnackbar(
                                    message = "Error: $signUpResponse",
                                    duration = SnackbarDuration.Long
                                )
                                println(signUpResponse)
                            }
                        }
                    }
                })
                .addOnFailureListener(OnFailureListener { e ->
                    if (e is ApiException) {
                        // An error occurred when communicating with the
                        // reCAPTCHA service. Refer to the status code to
                        // handle the error appropriately.
                        MainScope().async {
                            snackbarHost.showSnackbar(
                                message = "Error: ${
                                    CommonStatusCodes.getStatusCodeString(
                                        e.statusCode
                                    )
                                }"
                            )
                        }
                        println("Error: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}")
                    } else {
                        // A different, unknown type of error occurred.
                        MainScope().async {
                            snackbarHost.showSnackbar(
                                message = "Error: ${e.message}"
                            )
                        }
                        println("Error: ${e.message}")
                    }
                })
        }
    }, modifier = Modifier.padding(top = 5.dp)) {
        Text(text = "Sign up!", fontFamily = justFamily)
    }
}

@Composable
fun WelcomeKitmeTexts() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome to KitMe!",
            modifier = Modifier.padding(top = 10.dp, start = 5.dp),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = justFamily
        )
        Text(
            text = "Enjoy your stay with the cats!",
            modifier = Modifier.padding(top = 5.dp, start = 5.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = justFamily,
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun SignUpPreview() {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = remember {
                SnackbarHostState()
            })
        }
    ) {
        SignUpScreenContent(SignUpScreenViewModel(), rememberNavController(), remember {
            SnackbarHostState()
        })
    }
}
