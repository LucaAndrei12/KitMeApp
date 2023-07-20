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

val commentClient = LocalHttpClient().getClient()
val rootCommentUrl = "https://kitme.xyz:8443/kitme/api/comments"

//Returns "true" or an error message
fun addComment(token: String, content: String, postOID: String, scope: CoroutineScope): String{
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val addCommentResponse: HttpResponse = commentClient.submitForm(
            url = "$rootCommentUrl/addComment",
            formParameters = parameters {
                append("token", token)
                append("content", content)
                append("postOID", postOID)
            }
        )
        val responseDocument = Document.parse(addCommentResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Returns "true" or an error message
fun removeComment(token: String, commentOID: String, scope: CoroutineScope): String{
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val deleteCommentResponse: HttpResponse = commentClient.submitForm(
            url = "$rootCommentUrl/deleteComment",
            formParameters = parameters {
                append("token", token)
                append("commentOID", commentOID)
            }
        )
        val responseDocument = Document.parse(deleteCommentResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Returns "true" or an error message
fun likeCommmentAction(token: String, commentOID: String, isLike: Boolean, scope: CoroutineScope): String{
    var wasSuccessful = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val likeCommentResponse: HttpResponse = commentClient.submitForm(
            url = rootCommentUrl + if(isLike) "/likeComment" else "/dislikeComment",
            formParameters = parameters {
                append("token", token)
                append("commentOID", commentOID)
            }
        )
        val responseDocument = Document.parse(likeCommentResponse.body<String>())
        wasSuccessful = responseDocument.get("wasSuccessful") as String
    }
    return wasSuccessful
}

//Returns json with the post or an error message. To tell them apart, use contains("{")
fun getCommentInfo(commentOID: String, scope: CoroutineScope): String{
    var commentOrError = "An error occurred."
    scope.launch(Dispatchers.IO) {
        val getPostResponse = postClient.get("$rootCommentUrl/getCommentContent/$commentOID")
        if(getPostResponse.body<String>().contains("wasSuccessful")){
            val responseDocument = Document.parse(getPostResponse.body<String>())
            commentOrError  = responseDocument["wasSuccessful"] as String
        } else{
            commentOrError = getPostResponse.body()
        }
    }
    return commentOrError
}

