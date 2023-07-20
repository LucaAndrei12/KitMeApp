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
import java.util.Arrays


val postClient = LocalHttpClient().getClient()
val rootPostUrl = "https://kitme.xyz:8443/kitme/api/posts"

//Adds a post and returns "true" or an error message
fun addPost(token: String, base64image: String, title: String, scope: CoroutineScope): String{
    var wasSuccessful = "An error occurred."
    scope.launch {
        val addPostResponse: HttpResponse = postClient.submitForm(
            url = "$rootPostUrl/addPost",
            formParameters = parameters {
                append("token", token)
                append("base64image", base64image)
                append("title", title)
            }
        )
        val responseDocument = Document.parse(addPostResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Adds a post and returns "true" or an error message
fun removePost(token: String, postOID: String, scope: CoroutineScope): String{
    var wasSuccessful = "An error occurred."
    scope.launch {
        val removePostResponse: HttpResponse = postClient.submitForm(
            url = "$rootPostUrl/removePost",
            formParameters = parameters {
                append("token", token)
                append("postOID", postOID)
            }
        )
        val responseDocument = Document.parse(removePostResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Provide value "true" for like and "false" for dislike. Returns "true" or an error message.
fun likePostAction(value: Boolean, postOID: String, token: String, scope: CoroutineScope): String{
    var wasSuccessful = "An error occurred."
    scope.launch {
        val likeActionResponse: HttpResponse = postClient.submitForm(
            url = rootPostUrl + if(value) "/likePost" else "/dislikePost",
            formParameters = parameters {
                append("token", token)
                append("postOID", postOID)
            }
        )
        val responseDocument = Document.parse(likeActionResponse.body<String>())
        wasSuccessful = responseDocument["wasSuccessful"] as String
    }
    return wasSuccessful
}

//Returns arraylist with list of posts' JSONs OR a list with ONLY ONE ELEMENT, which is the error message. CHECK SIZE ON USE
fun getFeed(token: String, scope: CoroutineScope): ArrayList<String>{
    var arr: ArrayList<String> = ArrayList()
    scope.launch {
        val signUpResponse: HttpResponse = userClient.submitForm(
            url = "$rootUserUrl/getFeed",
            formParameters = parameters {
                append("token", token)
            }
        )

        if(signUpResponse.body<String>().contains("wasSuccessful")){
            val responseDocument = Document.parse(signUpResponse.body<String>())
            arr.add(responseDocument.get("wasSuccessful") as String)
        } else{
            arr = Arrays.asList(signUpResponse.body<String>().split(",")) as ArrayList<String>
        }
    }
    return arr
}

//Returns json with the post or an error message. To tell them apart, use contains("{")
fun getPostInfo(postOID: String, scope: CoroutineScope): String{
    var postOrError = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val getPostResponse = postClient.get("$rootPostUrl/getPostInfo/$postOID")
        if(getPostResponse.body<String>().contains("wasSuccessful")){
            val responseDocument = Document.parse(getPostResponse.body<String>())
            postOrError = responseDocument["wasSuccessful"] as String
        } else{
            postOrError = getPostResponse.body<String>()
        }
    }
    return postOrError
}
