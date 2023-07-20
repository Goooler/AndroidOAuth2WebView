package com.alessandrodefrancesco.utils

import android.util.Log
import com.google.gson.Gson
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer

class LoggingInterceptor : Interceptor {
    companion object {
        val TAG = "LoggingInterceptor"
        val dateFormatter = SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault())
        val gson = Gson()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {
        try {
            // REQUEST
            val request: Request = chain.request()

            val t1 = System.nanoTime()
            var requestLog = "\n${request.method().uppercase(Locale.getDefault())} (${dateFormatter.format(Date())}) ${request.url()}"
            if (request.body() != null) {
                requestLog += "\nWith body: ${bodyToString(request)}"
            }
            Log.d(TAG, requestLog)

            // RESPONSE
            val response = chain.proceed(request)
            val t2 = System.nanoTime()
            val millisecondsForResponse = TimeUnit.MILLISECONDS.convert((t2 - t1), TimeUnit.NANOSECONDS)

            val responseLog = "\nRESPONSE ${response.request().url()} in $millisecondsForResponse milliseconds (${dateFormatter.format(Date())})"
            val bodyString = response.body()?.string() ?: "NULL"

            largeLog(TAG, "$responseLog\n$bodyString")

            return response.newBuilder()
                .body(ResponseBody.create(response.body()!!.contentType(), bodyString))
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.localizedMessage)
            return chain.proceed(chain.request())
        }
    }

    private fun largeLog(tag: String, content: String) {
        if (content.length > 4000) {
            Log.d(tag, content.substring(0, 4000))
            largeLog(tag, content.substring(4000))
        } else {
            Log.d(tag, content)
        }
    }

    private fun bodyToString(request: Request): String? {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body()!!.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            null
        }
    }
}
