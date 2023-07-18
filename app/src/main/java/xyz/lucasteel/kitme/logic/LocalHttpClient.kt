package xyz.lucasteel.kitme.logic

import android.net.http.X509TrustManagerExtensions
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.network.tls.TLSConfigBuilder
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class LocalHttpClient {

    fun getClient() : HttpClient{
        return HttpClient(CIO){
            engine {
                https { trustManager = MyTrustManager(this)}
            }
        }
    }
}

//For this to work I added my certificate in the res/raw folder and also added this for something called "domain-specific certificates"
private class MyTrustManager(private val config: TLSConfigBuilder) : X509TrustManager {
    private val delegate = config.build().trustManager
    private val extensions = X509TrustManagerExtensions(delegate)

    override fun checkClientTrusted(certificates: Array<out X509Certificate>?, authType: String?) {}

    override fun checkServerTrusted(certificates: Array<out X509Certificate>?, authType: String?) {}

    override fun getAcceptedIssuers(): Array<X509Certificate> = delegate.acceptedIssuers
}