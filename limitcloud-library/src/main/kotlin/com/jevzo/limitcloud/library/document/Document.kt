package com.jevzo.limitcloud.library.document

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.*
import java.nio.charset.StandardCharsets

class Document(private val jsonObject: JsonObject) {

    constructor() : this(JsonObject())

    companion object {
        private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

        fun read(file: File): Document {
            val bufferedReader = BufferedReader(FileReader(file))
            val jsonObject = gson.fromJson(bufferedReader, JsonObject::class.java)

            return Document(jsonObject)
        }

        fun read(input: String): Document {
            val jsonObject = gson.fromJson(input, JsonObject::class.java)
            return Document(jsonObject)
        }
    }

    fun appendString(key: String, value: String): Document {
        jsonObject.addProperty(key, value)
        return this
    }

    fun appendInt(key: String, value: Int): Document {
        jsonObject.addProperty(key, value)
        return this
    }

    fun appendDouble(key: String, value: Double): Document {
        jsonObject.addProperty(key, value)
        return this
    }

    fun appendFloat(key: String, value: Float): Document {
        jsonObject.addProperty(key, value)
        return this
    }

    fun appendLong(key: String, value: Long): Document {
        jsonObject.addProperty(key, value)
        return this
    }

    fun appendBoolean(key: String, value: Boolean): Document {
        jsonObject.addProperty(key, value)
        return this
    }

    fun appendList(key: String, value: MutableList<*>): Document {
        jsonObject.add(key, gson.toJsonTree(value))
        return this
    }

    fun appendJsonElement(key: String, value: JsonElement): Document {
        jsonObject.add(key, value)
        return this
    }

    fun appendDocument(key: String, value: Document): Document {
        jsonObject.add(key, value.jsonObject)
        return this
    }

    fun appendMap(key: String, value: MutableMap<*, *>): Document {
        jsonObject.add(key, gson.toJsonTree(value))
        return this
    }

    fun removeEntry(key: String): Document {
        if (jsonObject.has(key)) jsonObject.remove(key)
        return this
    }

    fun getStringValue(key: String): String {
        if (!jsonObject.has(key)) return "null"
        return jsonObject.get(key).asString
    }

    fun getIntValue(key: String): Int {
        if (!jsonObject.has(key)) return -1
        return jsonObject.get(key).asInt
    }

    fun getDoubleValue(key: String): Double {
        if (!jsonObject.has(key)) return -1.0
        return jsonObject.get(key).asDouble
    }

    fun getFloatValue(key: String): Float {
        if (!jsonObject.has(key)) return -1.0F
        return jsonObject.get(key).asFloat
    }

    fun getLongValue(key: String): Long {
        if (!jsonObject.has(key)) return -1
        return jsonObject.get(key).asLong
    }

    fun getBooleanValue(key: String): Boolean {
        if (!jsonObject.has(key)) return false
        return jsonObject.get(key).asBoolean
    }

    fun getJsonElementValue(key: String): JsonElement {
        if (!jsonObject.has(key)) return JsonObject()
        return jsonObject.get(key)
    }

    fun getDocument(key: String): Document {
        if (!jsonObject.has(key)) return Document()
        return Document(jsonObject.get(key).asJsonObject)
    }

    fun getMap(key: String): MutableMap<*, *> {
        if (!jsonObject.has(key)) return mutableMapOf("" to "")
        return gson.fromJson(jsonObject.get(key), MutableMap::class.java)
    }

    fun getList(key: String): MutableList<*> {
        if (!jsonObject.has(key)) return mutableListOf("")
        return gson.fromJson(jsonObject.get(key), MutableList::class.java)
    }

    fun getAsJsonObject(): JsonObject {
        return jsonObject
    }

    fun getAsString(): String {
        return jsonObject.toString()
    }

    fun write(file: File) {
        val bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8))

        bufferedWriter.write(gson.toJson(jsonObject))
        bufferedWriter.flush()
        bufferedWriter.close()
    }
}