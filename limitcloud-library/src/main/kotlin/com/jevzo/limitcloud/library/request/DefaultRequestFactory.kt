package com.jevzo.limitcloud.library.request

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.jevzo.limitcloud.library.request.utils.ContentType
import com.jevzo.limitcloud.library.request.utils.RequestMethod
import com.jevzo.limitcloud.library.threading.ThreadPool
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Future

class DefaultRequestFactory : RequestFactory {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    private lateinit var httpUrlConnection: HttpURLConnection

    override fun newFactory(url: String): RequestFactory {
        HttpURLConnection.setFollowRedirects(true)
        httpUrlConnection = URL(url).openConnection() as HttpURLConnection
        return this
    }

    override fun setRequestProperty(key: String, value: String): RequestFactory {
        httpUrlConnection.setRequestProperty(key, value)
        return this
    }

    override fun setUseCache(useCache: Boolean): RequestFactory {
        httpUrlConnection.useCaches = useCache
        return this
    }

    override fun setContentType(contentType: ContentType): RequestFactory {
        httpUrlConnection.setRequestProperty("Content-Type", contentType.definedType)
        return this
    }

    override fun setRequestMethod(requestMethod: RequestMethod): RequestFactory {
        httpUrlConnection.requestMethod = requestMethod.definedName
        return this
    }

    override fun setReadTimeout(timeout: Int): RequestFactory {
        httpUrlConnection.readTimeout = timeout
        return this
    }

    override fun setConnectTimeout(timeout: Int): RequestFactory {
        httpUrlConnection.connectTimeout = timeout
        return this
    }

    override fun fire(): InputStream {
        httpUrlConnection.connect()
        return httpUrlConnection.inputStream
    }

    override fun fireAndForget() {
        httpUrlConnection.connect()
        httpUrlConnection.disconnect()
    }

    override fun fireAndProcess(): JsonObject {
        httpUrlConnection.connect()

        val inputStream = httpUrlConnection.inputStream
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val jsonObject = gson.fromJson(bufferedReader.readLine(), JsonObject::class.java)

        bufferedReader.close()
        httpUrlConnection.disconnect()

        return jsonObject
    }

    override fun fireAndProcessAsync(threadPool: ThreadPool): Future<JsonObject>? {
        threadPool.internalPool.submit<JsonObject> {
            httpUrlConnection.connect()

            val inputStream = httpUrlConnection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val jsonObject = gson.fromJson(bufferedReader.readLine(), JsonObject::class.java)

            bufferedReader.close()
            httpUrlConnection.disconnect()

            return@submit jsonObject
        }
        return null
    }
}