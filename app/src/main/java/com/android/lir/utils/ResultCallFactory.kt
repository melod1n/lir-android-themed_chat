package com.android.lir.utils

import android.util.Log
import okhttp3.Request
import okio.Timeout
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class ResultCallFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val rawReturnType: Class<*> = getRawType(returnType)
        if (rawReturnType == Call::class.java) {
            if (returnType is ParameterizedType) {
                val callInnerType: Type = getParameterUpperBound(0, returnType)
                if (getRawType(callInnerType) == Answer::class.java) {
                    if (callInnerType is ParameterizedType) {
                        val resultInnerType = getParameterUpperBound(0, callInnerType)
                        return ResultCallAdapter<Any?>(resultInnerType)
                    }
                    return ResultCallAdapter<Nothing>(Nothing::class.java)
                }
            }
        }
        return null
    }
}

internal abstract class CallDelegate<In, Out>(protected val proxy: Call<In>) : Call<Out> {

    override fun execute(): Response<Out> = throw NotImplementedError()

    final override fun enqueue(callback: Callback<Out>) = enqueueImpl(callback)

    final override fun clone(): Call<Out> = cloneImpl()

    override fun cancel() = proxy.cancel()

    override fun request(): Request = proxy.request()

    override fun isExecuted() = proxy.isExecuted

    override fun isCanceled() = proxy.isCanceled

    abstract fun enqueueImpl(callback: Callback<Out>)

    abstract fun cloneImpl(): Call<Out>
}

private class ResultCallAdapter<R>(private val type: Type) : CallAdapter<R, Call<Answer<R>>> {

    override fun responseType() = type

    override fun adapt(call: Call<R>): Call<Answer<R>> = ResultCall(call)
}

internal class ResultCall<T>(proxy: Call<T>) : CallDelegate<T, Answer<T>>(proxy) {

    override fun enqueueImpl(callback: Callback<Answer<T>>) {
        proxy.enqueue(ResultCallback(this, callback))
    }

    override fun cloneImpl(): ResultCall<T> {
        return ResultCall(proxy.clone())
    }

    private class ResultCallback<T>(
        private val proxy: ResultCall<T>,
        private val callback: Callback<Answer<T>>
    ) : Callback<T> {

        override fun onResponse(call: Call<T>, response: Response<T>) {
            val result: Answer<T> = if (response.isSuccessful)
                Answer.Success(response.body() as T)
            else Answer.Error(response.errorBody()?.string() ?: "")

            callback.onResponse(proxy, Response.success(result))
        }

        override fun onFailure(call: Call<T>, error: Throwable) {
//            val result = when (error) {

//                is HttpException -> Answer.Error("Что-то пошло не так")
//                else -> Answer.Error("Произошла ошибка")
//            }
            callback.onResponse(
                proxy,
                Response.success(Answer.Error(Log.getStackTraceString(error)))
            )
        }
    }

    override fun timeout(): Timeout {
        return proxy.timeout()
    }
}

sealed class Answer<out R> {
    data class Success<out T>(val data: T) : Answer<T>()
    data class Error(val errorString: String) : Answer<Nothing>()
}