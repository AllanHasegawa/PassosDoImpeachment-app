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

package com.hasegawa.diapp.cloud

import com.hasegawa.diapp.cloud.restservices.retrofit.RetrofitRestService
import com.hasegawa.diapp.domain.restservices.responses.NewsResponse
import com.hasegawa.diapp.domain.restservices.responses.StepLinkResponse
import com.hasegawa.diapp.domain.restservices.responses.StepResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
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
    fun buildResponse(body: String): MockResponse {
        val response = MockResponse()
        response.addHeader("Content-Type: application/json;charset=utf-8")
        response.body = Buffer().readFrom(body.byteInputStream())
        response.throttleBody(256, 1, TimeUnit.SECONDS)
        return response
    }

    @Test
    fun testGetNews() {
        val expectedNewsResponse =
                """[{"title":"titleA","date":1,"url":"http://urlA","tldr":"tldrA"},""" +
                        """{"title":"titleB","date":2,"url":"http://urlB","tldr":"tldrB"},""" +
                        """{"title":"titleC","date":3,"url":"http://urlC","tldr":"tldrC"},""" +
                        """{"title":"titleD","date":4,"url":"http://urlD","tldr":"tldrD"},""" +
                        """{"title":"titleE","date":5,"url":"http://urlE","tldr":"tldrE"}]"""
        val expectedNews = listOf(
                NewsResponse("titleA", "http://urlA", "tldrA", 1),
                NewsResponse("titleB", "http://urlB", "tldrB", 2),
                NewsResponse("titleC", "http://urlC", "tldrC", 3),
                NewsResponse("titleD", "http://urlD", "tldrD", 4),
                NewsResponse("titleE", "http://urlE", "tldrE", 5)
        )

        val server = MockWebServer()
        server.enqueue(buildResponse(expectedNewsResponse))
        server.start()

        val url = server.url("/")

        val restService = RetrofitRestService(url.toString())

        val barrier = CyclicBarrier(2)
        var result: List<NewsResponse>? = null
        val subscription = restService.getNews()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    result = it
                    barrier.await()
                }
        barrier.await(3, TimeUnit.SECONDS)
        assertThat(result, `is`(expectedNews))


        val request = server.takeRequest()
        assertThat(request.path, `is`("/importantNews"))
        server.shutdown()
        assertThat(subscription.isUnsubscribed, `is`(true))
    }

    @Test
    fun testGetSteps() {
        val expectedStepsResponse = "[" +
                """{"title":"StepA","description":"DescA","possibleDate":"PosA","position":1,"completed":true,"links":[{"title":"LA","url":"http://LA"}]},""" +
                """{"title":"StepB","description":"","possibleDate":"PosB","position":2,"completed":true,"links":[]},""" +
                """{"title":"StepC","description":"DescC","possibleDate":"PosC","position":3,"completed":false,"links":[]}""" +
                "]"

        val expectedSteps = listOf(
                StepResponse("StepA", "DescA", "PosA", 1, true, listOf(
                        StepLinkResponse("LA", "http://LA")
                )),
                StepResponse("StepB", "", "PosB", 2, true, ArrayList()),
                StepResponse("StepC", "DescC", "PosC", 3, false, ArrayList())
        )

        val server = MockWebServer()
        server.enqueue(buildResponse(expectedStepsResponse))
        server.start()

        val url = server.url("/")

        val restService = RetrofitRestService(url.toString())

        val barrier = CyclicBarrier(2)
        var result: List<StepResponse>? = null
        val subscription = restService.getSteps()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    result = it
                    barrier.await()
                }
        barrier.await(3, TimeUnit.SECONDS)
        assertThat(result, `is`(expectedSteps))


        val request = server.takeRequest()
        assertThat(request.path, `is`("/steps"))
        server.shutdown()
        assertThat(subscription.isUnsubscribed, `is`(true))
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
        var result = false
        val subscription = restService.postGCMToken(token)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    result = true
                    barrier.await()
                }
        barrier.await(3, TimeUnit.SECONDS)
        assertThat(result, `is`(true))

        val request = server.takeRequest()
        assertThat(request.path, `is`("/gcm/tokens"))

        assertThat(request.body.readUtf8(), `is`(expectedBody))
        server.shutdown()
        assertThat(subscription.isUnsubscribed, `is`(true))
    }
}