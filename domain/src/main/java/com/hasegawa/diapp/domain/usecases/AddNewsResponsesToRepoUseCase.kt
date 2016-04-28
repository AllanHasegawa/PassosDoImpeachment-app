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

package com.hasegawa.diapp.domain.usecases

import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.domain.repositories.NewsRepository
import com.hasegawa.diapp.domain.restservices.responses.NewsResponse
import com.hasegawa.diapp.domain.restservices.responses.toEntity
import rx.Observable

class AddNewsResponsesToRepoUseCase(val responses: List<NewsResponse>,
                                    val newsRepository: NewsRepository,
                                    executionThread: ExecutionThread,
                                    postExecutionThread: PostExecutionThread) :
        UseCase<List<NewsEntity>>(executionThread, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<List<NewsEntity>> {
        return Observable.just(responses)
                .map { it.map { it.toEntity(null) } }
                .zipWith(newsRepository.clearNews(), { responses, cleared -> responses })
                .flatMap { newsRepository.addAllNews(it) }
                .map { newsRepository.notifyChange(); it }
    }
}
