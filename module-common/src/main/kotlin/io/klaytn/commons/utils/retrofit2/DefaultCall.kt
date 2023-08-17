package io.klaytn.commons.utils.retrofit2

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DefaultCall<T>(
        private val response: T,
) : Call<T> {
    override fun clone(): Call<T> {
        TODO("Not yet implemented")
    }

    override fun execute(): Response<T> {
        return Response.success(response)
    }

    override fun enqueue(callback: Callback<T>) {
        TODO("Not yet implemented")
    }

    override fun isExecuted(): Boolean {
        TODO("Not yet implemented")
    }

    override fun cancel() {
        TODO("Not yet implemented")
    }

    override fun isCanceled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun request(): Request {
        TODO("Not yet implemented")
    }

    override fun timeout(): Timeout {
        TODO("Not yet implemented")
    }
}
