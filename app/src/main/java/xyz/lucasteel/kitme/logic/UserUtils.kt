package xyz.lucasteel.kitme.logic

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.bson.Document
import java.io.File
import java.io.FileWriter
import java.util.Scanner

val userClient = LocalHttpClient().getClient()
const val rootUserUrl = "https://kitme.xyz:8443/kitme/api/users"
const val fileName = "token-file"

//TODO: MAKE ALL METHODS SUSPEND

//Returns A JSON with the user credentials or an error message
fun getUser(userID: String, scope: CoroutineScope): String {
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val getUserResponse = userClient.get("$rootUserUrl/getUser/$userID")
        val responseDocument = Document.parse(getUserResponse.body<String>())
        if (responseDocument.toJson().contains("wasSuccessful")) {
            wasSuccessful = responseDocument.get("wasSuccessful") as String
        } else {
            wasSuccessful = responseDocument.toJson()
        }
    }
    return wasSuccessful
}

//Returns either "true" if the operation was successful or and error message - emails the user
fun forgotUsername(email: String, scope: CoroutineScope): String {
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val forgotUsernameResponse = userClient.get("$rootUserUrl/emailUsername/$email")
        val responseDocument = Document.parse(forgotUsernameResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Returns if the OTP provided is the right one for the user corresponding to the token
fun verifyOTP(scope: CoroutineScope, token: String, otp: Int): Boolean {
    var wasSuccessful = false
    scope.launch(Dispatchers.IO) {
        val verifyOTPResponse: HttpResponse = userClient.submitForm(
            url = "$rootUserUrl/verifyOTP",
            formParameters = parameters {
                append("token", token)
                append("otp", "$otp")
            }
        )
        val responseDocument = Document.parse(verifyOTPResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as Boolean
    }
    return wasSuccessful
}

//Returns either "true" if the operation was successful or and error message
fun updateOTP(scope: CoroutineScope, token: String): String {
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val updateOTPResponse: HttpResponse = userClient.submitForm(
            url = "$rootUserUrl/updateOTP",
            formParameters = parameters {
                append("token", token)
            }
        )
        val responseDocument = Document.parse(updateOTPResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Returns an error message or A JSON with the field "token" that contains the token
suspend fun login(
    scope: CoroutineScope,
    username: String,
    password: String,
    captcha: String
): String {
    var wasSuccessful: String
    val loginResponse: HttpResponse = scope.async {
        userClient.submitForm(
            url = "$rootUserUrl/login",
            formParameters = parameters {
                append("username", username)
                append("password", password)
                append("captchaToken", captcha)
            }
        )
    }.await()
    val responseDocument = Document.parse(loginResponse.body<String>())
    //Parses the response and chooses either the field "token" or "wasSuccessful" - I was dumb when I made the backend, sorry
    if (responseDocument.toJson().contains("token")) {
        wasSuccessful = responseDocument.toJson()
    } else if (responseDocument.toJson().contains("wasSuccessful")) {
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    } else {
        wasSuccessful = responseDocument.toJson()
    }
    return wasSuccessful
}

//Returns an error message or A JSON with the field "token" that contains the token
suspend fun signUp(
    scope: CoroutineScope,
    email: String,
    username: String,
    password: String,
    captcha: String
): String {
    var wasSuccessful : String
        val signUpResponse: HttpResponse = scope.async {
            userClient.submitForm(
                url = "$rootUserUrl/signUp",
                formParameters = parameters {
                    append("username", username)
                    append("password", password)
                    append("captchaToken", captcha)
                    append("email", email)
                }
            )
        }.await()
            val responseDocument = Document.parse(signUpResponse.body<String>())
            //Parses the response and chooses either the field "token" or "wasSuccessful" - I was dumb when I made the backend, sorry
            if (responseDocument.toJson().contains("token")) {
                wasSuccessful = responseDocument.toJson()
            } else if (responseDocument.toJson().contains("wasSuccessful")) {
                wasSuccessful = responseDocument.get("wasSuccessful") as String
            } else {
                wasSuccessful = responseDocument.toJson()
            }
    return wasSuccessful
}

//Returns true if the operation was successful and false if not. UPDATE OTP BEFORE USE !!!
fun forgotPassword(
    scope: CoroutineScope,
    username: String,
    otp: Int,
    newPassword: String
): Boolean {
    var wasSuccessful = false
    scope.launch(Dispatchers.IO) {
        val forgotPasswordResponse: HttpResponse = userClient.submitForm(
            url = "$rootUserUrl/forgotPassword",
            formParameters = parameters {
                append("usernameOrEmail", username)
                append("otp", "$otp")
                append("newPassword", newPassword)
            }
        )
        val responseDocument = Document.parse(forgotPasswordResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as Boolean
    }
    return wasSuccessful
}

//Returns either "true" if the operation was successful or and error message
fun updatePFP(scope: CoroutineScope, token: String, base64image: String): String {
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val updatePicture: HttpResponse = userClient.submitForm(
            url = "$rootUserUrl/updateProfilePicture",
            formParameters = parameters {
                append("token", token)
                append("base64image", base64image)
            }
        )
        val responseDocument = Document.parse(updatePicture.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Returns either "true" if the operation was successful or and error message
fun savePostAction(scope: CoroutineScope, token: String, postOID: String, save: Boolean): String {
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val saveActionResponse: HttpResponse = userClient.submitForm(
            url = rootUserUrl + if (save) "/savePost" else "/unsavePost",
            formParameters = parameters {
                append("token", token)
                append("postOID", postOID)
            }
        )
        val responseDocument = Document.parse(saveActionResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

fun saveTokenToFile(context: Context, token: String) {
    val tokenFile = File(context.filesDir, fileName)
    tokenFile.createNewFile()
    val fileWriter = FileWriter(tokenFile)
    fileWriter.write(token)
    fileWriter.close()
    fileWriter.flush()
}

fun isTokenAvailable(context: Context): Boolean {
    val tokenFile = File(context.filesDir, fileName)
    if (tokenFile.exists()) {
        val fileReader = Scanner(tokenFile)
        if (fileReader.hasNextLine()) {
            return true
        }
        return false
    } else {
        return false
    }
}

fun getToken(context: Context): String {
    val tokenFile = File(context.filesDir, fileName)
    val scanner = Scanner(tokenFile)
    scanner.close()
    return scanner.nextLine()
}








