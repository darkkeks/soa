package me.darkkeks.soa.voicechat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonUtils {
    private val mapper = ObjectMapper()
        .registerKotlinModule()

    fun toJson(value: Any): String = mapper.writeValueAsString(value)

    fun <T> fromJson(string: String, clazz: Class<T>): T = mapper.readValue(string, clazz)

    inline fun <reified T> fromJson(string: String): T = fromJson(string, T::class.java)
}
