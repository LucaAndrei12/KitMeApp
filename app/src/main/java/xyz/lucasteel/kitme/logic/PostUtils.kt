package xyz.lucasteel.kitme.logic

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
import kotlinx.coroutines.async
import org.bson.Document
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Arrays


val postClient = LocalHttpClient().getClient()
val rootPostUrl = "https://kitme.xyz:8443/kitme/api/posts"


//Adds a post and returns "true" or an error message
suspend fun addPost(
    token: String,
    image: ByteArray,
    title: String,
    scope: CoroutineScope
): String? {
    var wasSuccessful = "An error occurred."

    val addPostResponse: HttpResponse = scope.async {
        postClient.submitFormWithBinaryData(
            url = "$rootPostUrl/addPost",
            formData = formData {
                append("token", token)
                append("title", title)
                append("image", image, Headers.build {
                    append(HttpHeaders.ContentType, "image/png")
                    append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                })
            }
        )
    }.await()

    val responseDocument = Document.parse(addPostResponse.body<String>())
    return responseDocument.get("wasSuccessful") as String?
}

//Adds a post and returns "true" or an error message
suspend fun removePost(token: String, postOID: String, scope: CoroutineScope): String {
    var wasSuccessful = "An error occurred."

    val removePostResponse: HttpResponse = scope.async {
        postClient.submitForm(
            url = "$rootPostUrl/removePost",
            formParameters = parameters {
                append("token", token)
                append("postOID", postOID)
            }
        )
    }.await()
    val responseDocument = Document.parse(removePostResponse.body<String>())
    wasSuccessful = responseDocument.get("wasSuccessful") as String

    return wasSuccessful
}

//Provide value "true" for like and "false" for dislike. Returns "true" or an error message.
suspend fun likePostAction(
    value: Byte,
    postOID: String,
    token: String,
    scope: CoroutineScope
): String {
    var wasSuccessful = "An error occurred."

    val likeActionResponse: HttpResponse = scope.async {
        postClient.submitForm(
            url = "$rootPostUrl/likePostAction",
            formParameters = parameters {
                append("token", token)
                append("postOID", postOID)
                append("value", "$value")
            }
        )
    }.await()
    val responseDocument = Document.parse(likeActionResponse.body<String>())
    wasSuccessful = responseDocument["wasSuccessful"] as String

    return wasSuccessful
}

//Returns arraylist with list of posts' JSONs OR a list with ONLY ONE ELEMENT, which is the error message. CHECK SIZE ON USE
suspend fun getFeed(token: String, scope: CoroutineScope): ArrayList<String> {
    var arr: ArrayList<String> = ArrayList<String>()

    val getFeedResponse: HttpResponse = scope.async {
        userClient.submitForm(
            url = "$rootPostUrl/getFeed",
            formParameters = parameters {
                append("token", token)
            }
        )
    }.await()

    if (getFeedResponse.body<String>().contains("wasSuccessful")) {
        val responseDocument = Document.parse(getFeedResponse.body<String>())
        arr.add(responseDocument.get("wasSuccessful") as String + "while getting feed")
    } else {
        if (getFeedResponse.body<String>().contains("},{")) {
            val toAddToArray = getFeedResponse.body<String>().split("},{").toMutableList()
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
        } else {
           arr.add(getFeedResponse.body())
        }
    }
    return arr
}

//Returns json with the post or an error message. To tell them apart, use contains("{")
suspend fun getPostInfo(postOID: String, scope: CoroutineScope): String {
    var postOrError = "An error occurred."

    val getPostResponse = scope.async {
        postClient.get("$rootPostUrl/getPostInfo/$postOID")
    }.await()
    if (getPostResponse.body<String>().contains("wasSuccessful")) {
        val responseDocument = Document.parse(getPostResponse.body<String>())
        postOrError = responseDocument["wasSuccessful"] as String + "while getting post info"
    } else {
        postOrError = getPostResponse.body()
    }

    return postOrError
}

//Returns response JSON. IF contains "queryResult", good. IF contains "wasSuccessful", bad.
suspend fun search(isPostSearch: Boolean, query: String, scope: CoroutineScope): String{
    val searchResponse: HttpResponse = scope.async {
        postClient.submitForm(
            url = "$rootPostUrl/searchQuery",
            formParameters = parameters {
                append("isPostSearch", isPostSearch.toString())
                append("query", query)
            }
        )
    }.await()

    return searchResponse.body()
}


