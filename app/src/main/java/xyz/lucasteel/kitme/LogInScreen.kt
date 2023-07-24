package xyz.lucasteel.kitme

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
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
import kotlinx.coroutines.MainScope
import org.bson.Document
import xyz.lucasteel.kitme.logic.login
import xyz.lucasteel.kitme.logic.saveTokenToFile
import xyz.lucasteel.kitme.ui.theme.justFamily
import java.util.concurrent.Executor

const val CAPTCHA_SITE_KEY = "6LcBLd4mAAAAALJvh_I1u769wQ0HAze_DCw5vMmj"

@Composable
fun LogInScreen(navController: NavController) {
    val viewModel = LogInScreenViewModel()
    LoginScreenContent(viewModel, navController)
}

@Composable
fun LoginScreenContent(viewModel: LogInScreenViewModel, navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WelcomeLoginTexts()
            Column(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log in:",
                    modifier = Modifier
                        .padding(bottom = 5.dp, start = 5.dp)
                        .align(Alignment.Start),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = justFamily
                )
                UsernameTextField(viewModel = viewModel)
                PasswordTextField(viewModel = viewModel)
                LogInButton(viewModel = viewModel, navController = navController)
                HelpText(navController = navController)
            }
            Image(
                painter = painterResource(R.drawable.login_kitme_cropped),
                contentDescription = "kitten photos",
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun UsernameTextField(viewModel: LogInScreenViewModel) {
    OutlinedTextField(
        value = viewModel.usernameText.value,
        onValueChange = { viewModel.usernameText.value = it },
        label = { Text(text = "Username", fontFamily = justFamily) },
        singleLine = true
    )
}

@Composable
fun PasswordTextField(viewModel: LogInScreenViewModel) {
    OutlinedTextField(
        value = viewModel.passwordText.value,
        onValueChange = { viewModel.passwordText.value = it },
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

            IconButton(onClick = {
                viewModel.isPasswordVisible.value = !viewModel.isPasswordVisible.value
            }) {
                Icon(imageVector = image, description)
            }
        })
}

@Composable
fun LogInButton(viewModel: LogInScreenViewModel, navController: NavController) {
    val context = LocalContext.current
    Button(onClick = {
        SafetyNet.getClient(context).verifyWithRecaptcha(CAPTCHA_SITE_KEY)
            .addOnSuccessListener(OnSuccessListener { response ->
                // Indicates communication with reCAPTCHA service was
                // successful.
                val userResponseToken = response.tokenResult
                if (response.tokenResult?.isNotEmpty() == true) {
                    val loginResponse = login(MainScope(), viewModel.usernameText.value, viewModel.passwordText.value, userResponseToken!!)
                    if(loginResponse.contains("token")){
                        val token = Document.parse(loginResponse).get("token") as String
                        // saveTokenToFile(context, token)
                        navController.navigate("verifyOTP/$token")

                        //TODO: Add successful message to snackbar
                    }
                }
            })
            .addOnFailureListener(OnFailureListener { e ->
                if (e is ApiException) {
                    // An error occurred when communicating with the
                    // reCAPTCHA service. Refer to the status code to
                    // handle the error appropriately.
                    //TODO: ADD SNACKBAR WITH ERROR
                    println("Error: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}")
                } else {
                    // A different, unknown type of error occurred.
                    //TODO: ADD SNACKBAR WITH ERROR
                    println("Error: ${e.message}")
                }
            })
    }) {
        Text(text = "Log in!", fontFamily = justFamily)
    }
}

@Composable
fun WelcomeLoginTexts() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome back!",
            modifier = Modifier.padding(top = 10.dp, start = 5.dp),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = justFamily
        )
        Text(
            text = "We're so glad to see you again!",
            modifier = Modifier.padding(top = 5.dp, start = 5.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = justFamily,
        )
    }
}

@Composable
fun HelpText(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text =
            buildAnnotatedString {
                append("New to KitMe? ")
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Sign Up!")
                }
            },
            modifier = Modifier
                .padding(bottom = 5.dp, top = 15.dp)
                .clickable { navController.navigate("signUpScreen") },
            fontFamily = justFamily,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text =
            buildAnnotatedString {
                append("Forgot your username? ")
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Find it!")
                }
            },
            modifier = Modifier
                .padding(bottom = 5.dp)
                .clickable { navController.navigate("forgotUsernameScreen") },
            fontFamily = justFamily,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text =
            buildAnnotatedString {
                append("Forgot your password? ")
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Reset it!")
                }
            },
            modifier = Modifier
                .padding(bottom = 5.dp)
                .clickable { navController.navigate("forgotPasswordScreen") },
            fontFamily = justFamily,
            style = MaterialTheme.typography.bodyMedium
        )
    }

}

@Preview
@Composable
fun LogInScreenPreview() {
    LoginScreenContent(LogInScreenViewModel(), rememberNavController())
}