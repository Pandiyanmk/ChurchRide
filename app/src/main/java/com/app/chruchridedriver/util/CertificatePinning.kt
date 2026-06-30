package com.app.chruchridedriver.util

import android.content.Context
import com.app.chruchridedriver.R
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object CertificatePinning {
    fun getOKHttpClient(ctx: Context): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(60, TimeUnit.SECONDS)
        builder.readTimeout(60, TimeUnit.SECONDS)
        try {
            val cf = CertificateFactory.getInstance("X.509")
            val ca = cf.generateCertificate(ctx.resources.openRawResource(R.raw.apinpointio))
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry("ca", ca)
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.trustManagers, null)
            builder.sslSocketFactory(
                sslContext.socketFactory, tmf.trustManagers[0] as X509TrustManager
            )
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return builder
    }
}