import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object Utils {
    private val mapper = ObjectMapper()
        .registerKotlinModule()

    fun toJson(value: Any) = mapper.writeValueAsString(value)

    fun <T> fromJson(string: String, clazz: Class<T>) = mapper.readValue(string, clazz)

    inline fun <reified T> fromJson(string: String): T = fromJson(string, T::class.java)
}
