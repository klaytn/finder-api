package io.klaytn.commons.utils.okhttp

import io.klaytn.commons.utils.logback.logger
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient.Builder
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.Logger
import org.springframework.http.HttpHeaders
import java.net.Proxy
import java.util.concurrent.TimeUnit

private const val DEFAULT_MAX_REQUESTS = 10
private const val DEFAULT_MAX_REQUESTS_PER_HOST = 10
private const val DEFAULT_CONNECTION_TIMEOUT: Long = 1000
private const val DEFAULT_READ_TIMEOUT: Long = 3000
private const val DEFAULT_MAX_IDLE_CONNECTIONS = 120
private const val DEFAULT_KEEP_ALIVE_DURATION: Long = 10

val okhttpLogger: Logger = logger(HttpLoggingInterceptor::class.java)

val JSON = "application/json; charset=utf-8"
val FORM_URLENCODED = "application/x-www-form-urlencoded; charset=utf-8"

val debugHttpLoggingInterceptor = HttpLoggingInterceptor { okhttpLogger.debug(it) }.apply {
    setLevel(
        HttpLoggingInterceptor.Level.HEADERS
    )
}

class OkHttpClientBuilder(
    private var maxRequests: Int = DEFAULT_MAX_REQUESTS,
    private var maxRequestsPerHost: Int = DEFAULT_MAX_REQUESTS_PER_HOST,
    private var connectionTimeout: Long = DEFAULT_CONNECTION_TIMEOUT,
    private var readTimeout: Long = DEFAULT_READ_TIMEOUT,
    private var maxIdleConnections: Int = DEFAULT_MAX_IDLE_CONNECTIONS,
    private var keepAliveDuration: Long = DEFAULT_KEEP_ALIVE_DURATION,
    private var proxy: Proxy? = null,
) {
    private val builder = Builder()
    private val headers = mutableMapOf(HttpHeaders.CONTENT_TYPE to JSON)
    private val interceptors = mutableListOf<Interceptor>(debugHttpLoggingInterceptor)

    private val headerInterceptor
        get() = Interceptor { chain ->
            val builder = chain.request().newBuilder()
            headers.forEach {
                builder.addHeader(it.key, it.value)
            }
            chain.proceed(builder.build())
        }

    fun proxy(proxy: Proxy) = apply { this.proxy = proxy }

    fun maxRequests(maxRequests: Int) = apply { this.maxRequests = maxRequests }

    fun maxRequestsPerHost(maxRequestsPerHost: Int) = apply { this.maxRequestsPerHost = maxRequestsPerHost }

    fun connectionTimeout(connectionTimeout: Long) = apply { this.connectionTimeout = connectionTimeout }

    fun readTimeout(readTimeout: Long) = apply { this.readTimeout = readTimeout }

    fun maxIdleConnections(maxIdleConnections: Int) = apply { this.maxIdleConnections = maxIdleConnections }

    fun keepAliveDuration(keepAliveDuration: Long) = apply { this.keepAliveDuration = keepAliveDuration }

    fun addHeader(header: Pair<String, String>) = apply {
        headers[header.first] = header.second
    }

    fun addInterceptor(interceptor: Interceptor?) = apply {
        interceptor?.let { interceptors.add(it) }
    }

    private fun Builder.addInterceptors(interceptors: List<Interceptor>) = apply {
        interceptors.forEach { addInterceptor(it) }
    }

    fun build() = builder
        .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
        .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
        .connectionPool(ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MINUTES))
        .dispatcher(Dispatcher().apply {
            this.maxRequests = maxRequests
            this.maxRequestsPerHost = maxRequestsPerHost
        })
        .proxy(proxy)
        .addInterceptors(mutableListOf(headerInterceptor) + interceptors)
        .build()
}
