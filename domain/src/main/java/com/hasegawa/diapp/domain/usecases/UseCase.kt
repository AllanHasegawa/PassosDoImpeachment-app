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

import rx.Observable
import rx.Scheduler
import rx.Subscriber
import rx.subscriptions.Subscriptions

abstract class UseCase<T>(val subscribeOnThread: Scheduler, val postExecutionThread: Scheduler) {

    private var subscription = Subscriptions.empty()

    protected abstract fun buildUseCaseObservable(): Observable<T>

    fun execute(subscriber: Subscriber<T>) {
        subscription = buildUseCaseObservable()
                .subscribeOn(subscribeOnThread)
                .observeOn(postExecutionThread)
                .subscribe(subscriber)
    }

    fun unsubscribe() {
        if (!subscription.isUnsubscribed) {
            subscription.unsubscribe()
        }
    }
}
