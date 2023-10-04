package xyz.lucasteel.kitme.logic

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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

//Returns A JSON with the user credentials or an error message
suspend fun getUser(userID: String, scope: CoroutineScope): String {
    var wasSuccessful: String

        val getUserResponse = scope.async(Dispatchers.IO) {
            userClient.get("$rootUserUrl/getUser/$userID")
        }.await()
        val responseDocument = Document.parse(getUserResponse.body<String>())
        if (responseDocument.toJson().contains("wasSuccessful")) {
            wasSuccessful = responseDocument.get("wasSuccessful") as String + "while profile info"
        } else {
            wasSuccessful = responseDocument.toJson()
        }

    return wasSuccessful
}

//Returns oid as string or returns "false"
suspend fun getUserID(token: String, scope: CoroutineScope): String {
    val getOIDUser = scope.async(Dispatchers.IO) {
        userClient.get("$rootUserUrl/getOIDToken/$token")
    }.await()
    if(getOIDUser.body<String>().contains("wasSuccessful")){
        return "false"
    } else {
        return getOIDUser.body<String>()
    }
}

//Returns either "true" if the operation was successful or and error message - emails the user
suspend fun forgotUsername(email: String, scope: CoroutineScope): String {
    var wasSuccessful = "An error occurred."
        val forgotUsernameResponse = scope.async(Dispatchers.IO){
            userClient.get("$rootUserUrl/emailUsername/$email")
        }.await()
        val responseDocument = Document.parse(forgotUsernameResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    return wasSuccessful
}

//Returns if the OTP provided is the right one for the user corresponding to the token
suspend fun verifyOTP(scope: CoroutineScope, token: String, otp: Int): Boolean {
    var wasSuccessful: Boolean
    val verifyOTPResponse: HttpResponse = scope.async(Dispatchers.IO) {
        userClient.submitForm(
            url = "$rootUserUrl/verifyOTP",
            formParameters = parameters {
                append("token", token)
                append("otp", "$otp")
            }
        )
    }.await()

    val responseDocument = Document.parse(verifyOTPResponse.body<String>())
    wasSuccessful = responseDocument.get("wasSuccessful") as Boolean
    return wasSuccessful
}

//Returns if the OTP provided is the right one for the user corresponding to the token
suspend fun verifyOTPUsername(scope: CoroutineScope, username: String?, otp: Int): Boolean {
    var wasSuccessful: Boolean
    val verifyOTPResponse: HttpResponse = scope.async(Dispatchers.IO) {
        userClient.submitForm(
            url = "$rootUserUrl/verifyOTPUsername",
            formParameters = parameters {
                append("username", username!!)
                append("otp", "$otp")
            }
        )
    }.await()

    val responseDocument = Document.parse(verifyOTPResponse.body<String>())
    wasSuccessful = responseDocument.get("wasSuccessful") as Boolean
    return wasSuccessful
}

//Returns either "true" if the operation was successful or and error message
suspend fun updateOTP(scope: CoroutineScope, token: String): String {
    var wasSuccessful: String

    val updateOTPResponse: HttpResponse = scope.async(Dispatchers.IO) {
        userClient.submitForm(
            url = "$rootUserUrl/updateOTP",
            formParameters = parameters {
                append("token", token)
            }
        )
    }.await()
    val responseDocument = Document.parse(updateOTPResponse.body<String>())
    wasSuccessful = responseDocument.get("wasSuccessful") as String

    return wasSuccessful
}

//Returns either "true" if the operation was successful or and error message
suspend fun updateOTPUsername(scope: CoroutineScope, username: String?): String {
    var wasSuccessful: String

    val updateOTPResponse: HttpResponse = scope.async(Dispatchers.IO) {
        userClient.submitForm(
            url = "$rootUserUrl/updateOTPUsername",
            formParameters = parameters {
                append("username", username!!)
            }
        )
    }.await()
    val responseDocument = Document.parse(updateOTPResponse.body<String>())
    wasSuccessful = responseDocument.get("wasSuccessful") as String

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
    val loginResponse: HttpResponse = scope.async(Dispatchers.IO) {
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
    var wasSuccessful: String
    val signUpResponse: HttpResponse = scope.async(Dispatchers.IO) {
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
suspend fun forgotPassword(
    scope: CoroutineScope,
    username: String,
    otp: Int,
    newPassword: String
): Boolean {
    var wasSuccessful = false
        val forgotPasswordResponse: HttpResponse = scope.async(Dispatchers.IO) {
            userClient.submitForm(
                url = "$rootUserUrl/forgotPassword",
                formParameters = parameters {
                    append("usernameOrEmail", username)
                    append("otp", "$otp")
                    append("newPassword", newPassword)
                }
            )
        }.await()
        val responseDocument = Document.parse(forgotPasswordResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as Boolean
    return wasSuccessful
}

//Returns either "true" if the operation was successful or and error message
fun updatePFP(scope: CoroutineScope, token: String, image: ByteArray): String {
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val updatePicture: HttpResponse = userClient.submitFormWithBinaryData(
            url = "$rootUserUrl/updateProfilePicture",
            formData = formData {
                append("token", token)
                append("image", image, Headers.build {
                    append(HttpHeaders.ContentType, "image/png")
                    append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                })
            }
        )
        val responseDocument = Document.parse(updatePicture.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Returns either "true" if the operation was successful or and error message
suspend fun savePostAction(scope: CoroutineScope, token: String, postOID: String, save: Boolean): String {
    var wasSuccessful = "An error occurred."

        val saveActionResponse: HttpResponse = scope.async {
            userClient.submitForm(
                url = rootUserUrl + if (save) "/savePost" else "/unsavePost",
                formParameters = parameters {
                    append("token", token)
                    append("postOID", postOID)
                }
            )
        }.await()
        val responseDocument = Document.parse(saveActionResponse.body<String>())
    println(saveActionResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String

    return wasSuccessful
}
//Returns null if theres an error or an arraylist with the posts
suspend fun getSavedPosts(scope: CoroutineScope, token: String): ArrayList<String>?{
    val savedActionResponse: HttpResponse = scope.async {
        userClient.submitForm(
            url = "$rootUserUrl/getSavedPosts",
            formParameters = parameters {
                append("token", token)
            }
        )
    }.await()
    val arr: java.util.ArrayList<String> = ArrayList<String>()
    if (savedActionResponse.body<String>().contains("},{")) {
        val toAddToArray = savedActionResponse.body<String>().split("},{").toMutableList()
        for (item in toAddToArray) {
            if (item == toAddToArray[0]) {
                toAddToArray[0] = "$item}"
            } else if (item == toAddToArray[toAddToArray.size - 1]) {
                toAddToArray[toAddToArray.size - 1] = "{$item"
            } else {
                val indexOfItem = toAddToArray.indexOf(item)
                toAddToArray[indexOfItem] = "{$item}"
            }
        }
        arr.addAll(toAddToArray)
        return arr
    } else if(savedActionResponse.body<String>().contains("}")) {
        arr.add(savedActionResponse.body())
        return arr
    } else {
        return null
    }
}

fun saveTokenToFile(context: Context, token: String) {
    val tokenFile = File(context.filesDir, fileName)
    tokenFile.createNewFile()
    val fileWriter = FileWriter(tokenFile)
    fileWriter.write(token)
    fileWriter.flush()
    fileWriter.flush()
}


fun getToken(context: Context): String {
    val tokenFile = File(context.filesDir, fileName)
    if(!tokenFile.exists()){
        return ""
    }
    val scanner = Scanner(tokenFile)
    var content: String
    if(scanner.hasNextLine()){
        content = scanner.nextLine()
    } else {
        content = ""
    }

    scanner.close()
    return content
}








