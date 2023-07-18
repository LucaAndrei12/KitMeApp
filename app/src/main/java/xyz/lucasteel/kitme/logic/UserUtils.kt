package xyz.lucasteel.kitme.logic

import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.Document

//TODO:getUser() DONE
//TODO:forgotUsername() DONE
//TODO:verifyOTP() DONE
//TODO:login() done
//TODO:signUp() done
//TODO:forgotPassword() dONE
//TODO:updatePFP() Done
//TODO:updateOTP() DONE
//TODO:savePost() done
//TODO:unsavePost() done

val client = LocalHttpClient().getClient()
const val rootUserUrl = "https://kitme.xyz:8443/kitme/api/users"

//Returns A JSON with the user credentials or an error message
fun getUser(userID: String, scope: CoroutineScope): String{
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO){
        val getUserResponse = client.get("$rootUserUrl/getUser/$userID")
        val responseDocument = Document.parse(getUserResponse.body<String>())
        if (responseDocument.toJson().contains("wasSuccessful")) {
            wasSuccessful = responseDocument.get("wasSuccessful") as String
        } else{
            wasSuccessful = responseDocument.toJson()
        }
    }
    return wasSuccessful
}

//Returns either "true" if the operation was successful or and error message - emails the user
fun forgotUsername(email: String, scope: CoroutineScope): String{
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO){
        val forgotUsernameResponse = client.get("$rootUserUrl/emailUsername/$email")
        val responseDocument = Document.parse(forgotUsernameResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Returns if the OTP provided is the right one for the user corresponding to the token
fun verifyOTP(scope: CoroutineScope, token: String, otp: Int): Boolean{
    var wasSuccessful = false
    scope.launch {
        val verifyOTPResponse: HttpResponse = client.submitForm(
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
fun updateOTP(scope: CoroutineScope, token: String): String{
    var wasSuccessful = "An error occurred."
    scope.launch {
        val updateOTPResponse: HttpResponse = client.submitForm(
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
fun login(scope: CoroutineScope, username: String, password: String, captcha: String): String{
    var wasSuccessful = "An error occurred."
    scope.launch {
        val loginResponse: HttpResponse = client.submitForm(
            url = "$rootUserUrl/login",
            formParameters = parameters {
                append("username", username)
                append("password", password)
                append("captchaToken", captcha)
            }
        )
        val responseDocument = Document.parse(loginResponse.body<String>())
        //Parses the response and chooses either the field "token" or "wasSuccessful" - I was dumb when I made the backend, sorry
        if(responseDocument.toJson().contains("token")){
            wasSuccessful = responseDocument as String
        } else if(responseDocument.toJson().contains("wasSuccessful")){
            wasSuccessful = responseDocument.get("wasSuccessful") as String
        } else{
            wasSuccessful = responseDocument.toJson()
        }
    }
    return wasSuccessful
}

//Returns an error message or A JSON with the field "token" that contains the token
fun signUp(scope: CoroutineScope, email: String, username: String, password: String, captcha: String): String{
    var wasSuccessful = "An error occurred."
    scope.launch {
        val signUpResponse: HttpResponse = client.submitForm(
            url = "$rootUserUrl/signUp",
            formParameters = parameters {
                append("username", username)
                append("password", password)
                append("captchaToken", captcha)
                append("email", email)
            }
        )
        val responseDocument = Document.parse(signUpResponse.body<String>())
        //Parses the response and chooses either the field "token" or "wasSuccessful" - I was dumb when I made the backend, sorry
        if(responseDocument.toJson().contains("token")){
            wasSuccessful = responseDocument as String
        } else if(responseDocument.toJson().contains("wasSuccessful")){
            wasSuccessful = responseDocument.get("wasSuccessful") as String
        } else{
            wasSuccessful = responseDocument.toJson()
        }
    }
    return wasSuccessful
}

//Returns true if the operation was successful and false if not. UPDATE OTP BEFORE USE !!!
fun forgotPassword(scope: CoroutineScope, username: String, otp: Int, newPassword: String): Boolean{
    var wasSuccessful = false
    scope.launch(Dispatchers.IO){
        val forgotPasswordResponse: HttpResponse = client.submitForm (
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
fun updatePFP(scope: CoroutineScope, token: String, base64image: String): String{
    var wasSuccessful = "An error occurred."
    scope.launch {
        val updatePicture: HttpResponse = client.submitForm(
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
fun savePostAction(scope: CoroutineScope, token: String, postOID: String , save: Boolean): String{
    var wasSuccessful = "An error occurred."
    scope.launch {
        val saveActionResponse: HttpResponse = client.submitForm(
            url = rootUserUrl + if(save) "/savePost" else "/unsavePost",
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









