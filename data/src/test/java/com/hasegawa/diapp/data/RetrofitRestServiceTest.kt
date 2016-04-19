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

package com.hasegawa.diapp.data

import com.google.gson.Gson
import com.hasegawa.diapp.data.restservices.impls.retrofit.RetrofitRestService
import com.hasegawa.diapp.data.restservices.responses.NewsResponse
import com.hasegawa.diapp.data.restservices.responses.StepLinkResponse
import com.hasegawa.diapp.data.restservices.responses.StepResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import rx.schedulers.Schedulers
import java.util.ArrayList
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit


@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class RetrofitRestServiceTest {
    fun <T> buildResponse(body: T): MockResponse {
        val response = MockResponse()
        response.addHeader("Content-Type: application/json;charset=utf-8")
        val responseBody = Gson().toJson(body)
        response.body = Buffer().readFrom(responseBody.byteInputStream())
        response.throttleBody(256, 1, TimeUnit.SECONDS)
        return response
    }

    @Test
    fun testGetNews() {
        val expectedNews = listOf(
                NewsResponse("A", "urlA", "tldrA"),
                NewsResponse("B", "urlB", "tldrB"),
                NewsResponse("C", "urlC", "tldrC"),
                NewsResponse("D", "urlD", "tldrD"),
                NewsResponse("E", "urlE", "tldrE")
        )

        val server = MockWebServer()
        server.enqueue(buildResponse(expectedNews))
        server.start()

        val url = server.url("/")

        val restService = RetrofitRestService(url.toString())

        val barrier = CyclicBarrier(2)
        val subscription = restService.getNews()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Assert.assertEquals(expectedNews, it)
                    barrier.await()
                }
        barrier.await(10, TimeUnit.SECONDS)
        val request = server.takeRequest()
        Assert.assertEquals("/important_news", request.path)
        server.shutdown()
        Assert.assertEquals(true, subscription.isUnsubscribed)
    }

    @Test
    fun testGetSteps() {
        val expectedNews = listOf(
                StepResponse("A", "descA", 1, listOf(
                        StepLinkResponse("LinkA", "UrlA"),
                        StepLinkResponse("LinkB", "UrlB"),
                        StepLinkResponse("LinkC", "UrlC")
                )),
                StepResponse("B", "descB", 2, null),
                StepResponse("C", "descC", 3, ArrayList())
        )

        val server = MockWebServer()
        server.enqueue(buildResponse(expectedNews))
        server.start()

        val url = server.url("/")

        val restService = RetrofitRestService(url.toString())

        val barrier = CyclicBarrier(2)
        val subscription = restService.getSteps()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Assert.assertEquals(expectedNews, it)
                    barrier.await()
                }
        barrier.await(10, TimeUnit.SECONDS)
        val request = server.takeRequest()
        Assert.assertEquals("/steps", request.path)
        server.shutdown()
        Assert.assertEquals(true, subscription.isUnsubscribed)
    }

    @Test
    fun testPostToken() {

        val token = "ABCTOKEN :)"
        val expectedBody = """{"token":"$token"}"""

        val server = MockWebServer()
        server.enqueue(buildResponse(""))
        server.start()

        val url = server.url("/")

        val restService = RetrofitRestService(url.toString())

        val barrier = CyclicBarrier(2)
        val subscription = restService.postGCMToken(token)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    barrier.await()
                }
        barrier.await(10, TimeUnit.SECONDS)
        val request = server.takeRequest()
        Assert.assertEquals("/gcm/tokens", request.path)
        Assert.assertEquals(expectedBody, request.body.readUtf8())
        server.shutdown()
        Assert.assertEquals(true, subscription.isUnsubscribed)
    }
}