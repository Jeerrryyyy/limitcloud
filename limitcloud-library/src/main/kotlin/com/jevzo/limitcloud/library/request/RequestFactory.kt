package com.jevzo.limitcloud.library.request

import com.google.gson.JsonObject
import com.jevzo.limitcloud.library.request.utils.ContentType
import com.jevzo.limitcloud.library.request.utils.RequestMethod
import com.jevzo.limitcloud.library.threading.ThreadPool
import java.io.InputStream
import java.util.concurrent.Future

interface RequestFactory {

    fun newFactory(url: String): RequestFactory
    fun setRequestProperty(key: String, value: String): RequestFactory
    fun setUseCache(useCache: Boolean): RequestFactory
    fun setContentType(contentType: ContentType): RequestFactory
    fun setRequestMethod(requestMethod: RequestMethod): RequestFactory
    fun setReadTimeout(timeout: Int): RequestFactory
    fun setConnectTimeout(timeout: Int): RequestFactory
    fun fire(): InputStream
    fun fireAndForget()
    fun fireAndProcess(): JsonObject
    fun fireAndProcessAsync(threadPool: ThreadPool): Future<JsonObject>?
}