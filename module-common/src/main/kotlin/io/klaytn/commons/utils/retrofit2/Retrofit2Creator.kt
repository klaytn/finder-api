package io.klaytn.commons.utils.retrofit2

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.retrofit.CircuitBreakerCallAdapter
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class Retrofit2Creator<T : Any>(
        client: OkHttpClient,
        url: String,
        objectMapper: ObjectMapper,
        private val clazz: KClass<T>,
) {
    private val builder =
            Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .addConverterFactory(QueryStringConverterFactory.create())
                    .client(client)

    fun withCircuitBreaker(circuitBreakerConfig: CircuitBreakerConfig) = apply {
        builder.addCallAdapterFactory(
                CircuitBreakerCallAdapter.of(CircuitBreaker.of("retrofit2", circuitBreakerConfig))
        )
    }

    fun create(): T = builder.build().create(clazz.java)
}

class QueryStringConverterFactory : Converter.Factory() {
    companion object {
        fun create() = QueryStringConverterFactory()
    }

    override fun stringConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit,
    ): Converter<*, String>? {
        if (type == LocalDateTime::class.java) {
            return LocalDateTimeQueryConverter()
        }

        return super.stringConverter(type, annotations, retrofit)
    }
}

class LocalDateTimeQueryConverter : Converter<LocalDateTime, String> {
    override fun convert(value: LocalDateTime): String? {
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}

inline fun <reified T> Call<T>.orElseThrow(handleException: (t: String) -> Throwable): T =
        this.execute().let {
            if (it.isSuccessful) it.body()!! else throw handleException(it.errorBody()!!.string())
        }

inline fun <reified T> Call<T>.call(): T =
        this.execute().let {
            if (it.isSuccessful) it.body()!!
            else throw Retrofit2CallException(it.code(), it.errorBody()?.string())
        }

class Retrofit2CallException(code: Int, body: String?) : RuntimeException("status: $code\n$body")
