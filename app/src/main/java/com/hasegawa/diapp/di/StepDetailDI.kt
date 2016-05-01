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

package com.hasegawa.diapp.di

import com.hasegawa.diapp.controllers.StepDetailController
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Scope


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerStepDetail

@Component(modules = arrayOf(StepDetailModule::class),
        dependencies = arrayOf(ActivityComponent::class))
@PerStepDetail
interface StepDetailComponent {
    fun inject(s: StepDetailController)
}

@Module
class StepDetailModule(val stepPosition: Int) {
    @Provides @PerStepDetail @Named("stepPosition")
    fun stepPosition() = stepPosition
}
