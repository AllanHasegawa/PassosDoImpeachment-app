/*
 * Copyright 2016 Allan Yoshio Hasegawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hasegawa.diapp.cloud.restservices.retrofit

import android.accounts.NetworkErrorException
import com.hasegawa.diapp.domain.restservices.RestService
import com.hasegawa.diapp.domain.restservices.responses.NewsResponse
import com.hasegawa.diapp.domain.restservices.responses.StepResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import rx.Observable

class RetrofitRestService(url: String) : RestService {

    private val retrofit: Retrofit
    private val calls: RetrofitCalls

    init {
        var newUrl = url
        if (!url.endsWith('/')) {
            newUrl = "$newUrl/"
        }
        retrofit = Builder()
                .baseUrl(newUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        calls = retrofit.create(RetrofitCalls::class.java)
    }

    override fun getNews(): Observable<List<NewsResponse>> {
        val newsCall = calls.callGetNews()
        return Observable.fromCallable { newsCall.execute() }
                .map {
                    if (!it.isSuccessful) {
                        throw NetworkErrorException("${it.code()}: ${it.errorBody().string()}")
                    }
                    it.body()
                }
    }

    override fun getSteps(): Observable<List<StepResponse>> {
        val stepsCall = calls.callGetSteps()
        return Observable.fromCallable { stepsCall.execute() }
                .map {
                    if (!it.isSuccessful) {
                        throw NetworkErrorException("${it.code()}: ${it.errorBody().string()}")
                    }
                    it.body()
                }
    }

    override fun postGCMToken(token: String): Observable<Boolean> {
        val requestBody = TokenPost(token)
        val tokenCall = calls.callPostGcmTokenPost(requestBody)
        return Observable.fromCallable { tokenCall.execute() }
                .map { it.isSuccessful }
    }

    private data class TokenPost(val token: String? = null)

    private interface RetrofitCalls {
        @GET("importantNews")
        fun callGetNews(): Call<List<NewsResponse>>

        @GET("steps")
        fun callGetSteps(): Call<List<StepResponse>>

        @POST("gcm/tokens")
        fun callPostGcmTokenPost(@Body body: TokenPost): Call<ResponseBody>
    }
}

