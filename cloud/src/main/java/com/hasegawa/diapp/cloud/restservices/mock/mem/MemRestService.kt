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

package com.hasegawa.diapp.cloud.restservices.mock.mem

import com.hasegawa.diapp.domain.restservices.RestService
import com.hasegawa.diapp.domain.restservices.responses.NewsResponse
import com.hasegawa.diapp.domain.restservices.responses.StepResponse
import rx.Observable

class MemRestService : RestService {
    override fun getNews(): Observable<List<NewsResponse>> {
        return Observable.just(newsResponses)
    }

    override fun getSteps(): Observable<List<StepResponse>> {
        return Observable.just(stepResponses)
    }

    override fun postGCMToken(token: String): Observable<Boolean> {
        return Observable.just(tokenSuccess)
    }

    companion object {
        var newsResponses = emptyList<NewsResponse>()
        var stepResponses = emptyList<StepResponse>()
        var tokenSuccess = true
    }
}
