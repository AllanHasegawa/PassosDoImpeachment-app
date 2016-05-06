# Passos do Impeachment (Android app)

[![Play Link](http://steverichey.github.io/google-play-badge-svg/img/en_get.svg)](https://play.google.com/store/apps/details?id=com.hasegawa.diapp)

A *full* **demo** Android app showcasing data syncing with a RESTful server. Android [SyncAdapter](http://developer.android.com/training/sync-adapters/creating-sync-adapter.html) is used to schedule the appropriate time to sync the data, a process done with Retrofit. StorIO caches the data for offline usage. To keep devices up-to-date, the server issues GCM messages to the client when things changes. Kotlin and RxJava makes everything beautiful.

In short, the following awesome projects were used:

  * [Kotlin](https://kotlinlang.org/)
  * [RxJava](https://github.com/ReactiveX/RxJava)
  * [Conductor](https://github.com/bluelinelabs/Conductor) (for single-activity app)
  * [Dagger2](http://google.github.io/dagger/)
  * [StorIO](https://github.com/pushtorefresh/storio)
  * [Retrofit](http://square.github.io/retrofit/)
  * [Google Cloud Messaging (GCM)](https://developers.google.com/cloud-messaging/)
  * [Espresso](https://google.github.io/android-testing-support-library/docs/espresso/)
  * [Robolectric](http://robolectric.org/)
  * [Mockito](http://mockito.org/)
  * [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver)

The app has an accompanying **server** to sync the data with. Check it here: [PassosDoImpeachment-server](https://github.com/AranHase/PassosDoImpeachment-server)

<img alt="Phone1" title="Diagram" src="https://cdn.rawgit.com/AranHase/PassosDoImpeachment-app/d2d56c5022609835f6bc9c2a979d97fe6db0f1c5/resources/graphics/screenshots/2016_04_12/Screenshot_20160412-142845.png" width="180">
<img alt="Phone2" title="Diagram" src="https://cdn.rawgit.com/AranHase/PassosDoImpeachment-app/d2d56c5022609835f6bc9c2a979d97fe6db0f1c5/resources/graphics/screenshots/2016_04_12/Screenshot_20160412-142856.png" width="180">
<img alt="Phone3" title="Diagram" src="https://cdn.rawgit.com/AranHase/PassosDoImpeachment-app/d2d56c5022609835f6bc9c2a979d97fe6db0f1c5/resources/graphics/screenshots/2016_04_12/Screenshot_20160412-142901.png" width="180">
<img alt="Tablet1" title="Diagram" src="https://cdn.rawgit.com/AranHase/PassosDoImpeachment-app/d2d56c5022609835f6bc9c2a979d97fe6db0f1c5/resources/graphics/screenshots/2016_04_12/Screenshot_20160412-142957.png" width="360">

---


## Architecture

The architecture adopted in this project was heavily based on the [Android-CleanArchitecture](https://github.com/android10/Android-CleanArchitecture) project.
A more in-depth view of that architecture can be found in:

 * http://fernandocejas.com/2014/09/03/architecting-android-the-clean-way/
 * http://fernandocejas.com/2015/07/18/architecting-android-the-evolution/

A layered approach is used to better understand the architecture. The following image shows the three layers (like in an onion) used in this app:

![Layers](https://cdn.rawgit.com/AranHase/PassosDoImpeachment-app/da6e00c9430a484307fb582fd02c3a1abbc4cb59/resources/graphics/diagrams/readme_layers.svg)

A quick description of each layer will be given soon. But, the important thing to take from this image is how the **dependency arrow only points inwards**.
In other words, the *domain* layer doesn't known a thing about the *presentation* layer. However, the *presentation* depends on the *domain* layer.

Each layer can have many **modules** (as in a gradle module), and those modules are written in the image above. For instance,
the *domain* layer has just the *domain* module. However, the *app* layer has the *app*, *db* and *cloud* modules. 


#### domain layer

This is where the **business logic** resides. Most of the logic on how entities talk with each other and data manipulations happens here.

One concrete example: You got a list of responses from the cloud as json objects and you want to cache it with the app's local storage.
The *domain* layer will provide a *use case* (*interactors*) to the rest of the application with that functionality, as in [this piece of code](https://github.com/AranHase/PassosDoImpeachment-app/blob/72daa563dee8726369998a850adb0d26cb394ff3/domain/src/main/java/com/hasegawa/diapp/domain/usecases/AddNewsResponsesToRepoUseCase.kt#L33).

Important points:

 * No dependencies to others layers
 * Try to keep as minimum as possible the dependencies to others frameworks/libraries/tech

By not adding dependencies here you can **delay the decision** on what framework/libraries/tech to use. The decision on what database (Realm, SQLite, etc) should not matter at this point.
And, if one doesn't work well for your project, switching it around will be less painful.

However, there are times when you need some functionality from a framework (Android for example).
And when these times come, you **invert the dependency** (a.k.a. *Dependency Inversion Principle*).

In practice, let's say you want to share a text. The *domain* layer can just create an interface, as in [this piece of code](https://github.com/AranHase/PassosDoImpeachment-app/blob/af6994272bc2e81f9df03a2a18e3e7248c277cd4/domain/src/main/java/com/hasegawa/diapp/domain/devices/TextSharer.kt#L20),
and use it when needed without worrying what is implementing it. So another layer, the *app* for instance, can then step up and implement it, [like this](https://github.com/AranHase/PassosDoImpeachment-app/blob/72daa563dee8726369998a850adb0d26cb394ff3/app/src/main/java/com/hasegawa/diapp/devices/AppTextSharer.kt#L30).

#### presentation layer

The *presentation* layer depends on the *domain* layer. But, as in the *domain* layer, the *presentation* also tries to keep the number of dependencies to a minimum.
With that, the *presentation* does not depends on any framework (like Android).

This layer is mainly made of two types of objects:

 * **Presenter**: A bridge between the `MvpView` and the *domain* layer. It holds the logic to control the view, and uses the *domain* layer to get the data.
 It also has logic to deal with inputs from users (can be a person, or any external system).
 
 * **MvpView**: Interface or abstract class with no logic at all. All it does is to describe all the actions the *presenter* can do on a view, and what external events it can receive.
 In practice, this view will be implemented by the *app* layer.

#### app layer

The *app* layer is where we start to see more specific implementations. This layer has three modules:

 * *db*: Implements the repositories.
 * *cloud*: Implements the rest clients.
 * *app*: The *app* module is where most of the code goes. In here we will find the `MainActivity`,
 Android services (`GCMRegistration`, [SyncAdapters](http://developer.android.com/training/sync-adapters/creating-sync-adapter.html), etc)
 and Android views. 
 
---

### Architecture Diagram

The following diagram shows in more detail the different parts of the architecture and how they relate to each other:

<img alt="Diagram" title="Diagram" src="https://cdn.rawgit.com/AranHase/PassosDoImpeachment-app/da6e00c9430a484307fb582fd02c3a1abbc4cb59/resources/graphics/diagrams/readme_architecture.svg" width="720">

#### domain module

In the *domain* layer/module we have many interfaces. As commented early, its so we don't worry about specific implementations here.

The *use cases* are concrete classes, and they are the ones that will **interact with the rest of the system**.
All *use cases* implements an abstract [UseCase](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/domain/src/main/java/com/hasegawa/diapp/domain/usecases/UseCase.kt).
This implementation was heavily based on the one used by [Fernando Cejas](http://fernandocejas.com/2015/07/18/architecting-android-the-evolution/).

I really liked his design. Instead of the use case returning an `Observable`, he forces the user to pass a subscriber instead.
This way, if the user decides to do some more complex logic, it will be kind awkward, so it is better to just create another use case.


### cloud and db modules

Those two modules are really boring. All they do is implement some of the interfaces found in the *domain* layer.

The *db* module has two implementations, one using [Content Providers](http://developer.android.com/guide/topics/providers/content-providers.html)
([example](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/db/src/main/java/com/hasegawa/diapp/db/repositories/contentprovider/ContentProviderStepsRepository.kt))
and another [In-memory](https://en.wikipedia.org/wiki/In-memory_database)
([example](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/db/src/main/java/com/hasegawa/diapp/db/repositories/mocks/mem/MemStepsRepository.kt)).
The *cloud* module also has two implementations, one using [Retrofit](http://square.github.io/retrofit/)
([example](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/cloud/src/main/java/com/hasegawa/diapp/cloud/restservices/retrofit/RetrofitRestService.kt))
and another mock implementation that only replays what we want ([example](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/cloud/src/main/java/com/hasegawa/diapp/cloud/restservices/mock/mem/MemRestService.kt)).

### presentation module

The *presentation* module is based on the [MVP design pattern](http://lenguyenthanh.com/model-view-presentermvp-definitions-and-best-practices/).
*Presenters* will mostly call *use cases* and control the [MvpView](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/presentation/src/main/java/com/hasegawa/diapp/presentation/views/MvpView.kt)
([example](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/presentation/src/main/java/com/hasegawa/diapp/presentation/views/ListNewsMvpView.kt)).
Sometimes the *presenter* may use a *device*, but its communication is mostly limited to the view and the *use cases*. **Note**, this means the controller NEVER talks directly with
the presenter, even thought the controller does keep an instance of a presenter. The controller will implement the `MvpView` in the *app* module,
and will communicate with the presenter only through listeners in the `MvpView`.

The "Clean-way Android architecture" implementation I mentioned earlier keeps presentation + app on the same module.
But, why put the *presentation* module on a different module than the app? To me it was because my *presentation* module has no dependencies
to the Android framework. Also, keeping things separated was much easier to keep track of things and have an idea of what every screen and view will
have to do.

The view itself does not depend on any *presenter*, so the *presenter* needs to subscribe to listeners in the view to react to events from the view.

A concrete example: The `ListNewsPresenter` ([go to code](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/presentation/src/main/java/com/hasegawa/diapp/presentation/presenters/ListNewsPresenter.kt))
reactively loads the news and send a list to the `ListNewsMvpView` ([go to code](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/presentation/src/main/java/com/hasegawa/diapp/presentation/views/ListNewsMvpView.kt))
to be shown on screen. The view has listeners for when buttons are clicked, and the *presenter* can listen to those touches and react accordingly.

### app module

The *app* module is composed of a single activity named `MainActivity`. It uses [Conductor](https://github.com/bluelinelabs/Conductor) to control the screens (the views the users can see).
With Conductor, a **controller** can be like a fragment or an activity. A controller will have a *presenter* (but will not communicate with it) and will implement a *view*.

The controller is like a bridge that connects the `MvpView` from the *presentation* layer to Android Views (like a `ViewPager` or a `TextView`).

The only way for a controller to communicate with a presenter is through listeners in the view, this way the presenter can keep track of what is going on.

The services that run on background, like [synchronization](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/app/src/main/java/com/hasegawa/diapp/syncadapters/SyncAdapter.kt)
and
[GCM registration](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/app/src/main/java/com/hasegawa/diapp/services/GCMRegistrationService.kt),
will mostly delegate its work to *use cases*.
Also in this layer you will find the implementations for the [devices](https://github.com/AranHase/PassosDoImpeachment-app/tree/master/app/src/main/java/com/hasegawa/diapp/devices) interfaces.

---

## Reactive

The entire architecture is built from the ground up to support reactive programming. Let's see how the list of news is automatically update when the data changes.

First the *domain* declares repositories that return [Observables](http://reactivex.io/documentation/observable.html)
([NewsRepository](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/domain/src/main/java/com/hasegawa/diapp/domain/repositories/NewsRepository.kt)).

The [GetNewsUseCase](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/domain/src/main/java/com/hasegawa/diapp/domain/usecases/GetNewsUseCase.kt) will just return the same observable from the repository.
So the repository implementation must be aware of it. For the [Content Provider version](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/db/src/main/java/com/hasegawa/diapp/db/repositories/contentprovider/ContentProviderNewsRepository.kt)
[StorIO](https://github.com/pushtorefresh/storio) was used as it provides reactive queries.
For the [In-memory db](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/db/src/main/java/com/hasegawa/diapp/db/repositories/mocks/mem/MemNewsRepository.kt),
the behavior was simulated using [BehaviorSubjects](http://reactivex.io/documentation/subject.html).

The [ListNewsPresenter](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/presentation/src/main/java/com/hasegawa/diapp/presentation/presenters/ListNewsPresenter.kt) will subscribe to the
`GetNewsUseCase` and group the news by date prior to sending them to the `MvpView`. This operation is reactively done every time the data changes.

The `MvpView` is implemented by the [ListNewsController](https://github.com/AranHase/PassosDoImpeachment-app/blob/5513cd8e7249b92844dac1000ab781604cb50298/app/src/main/java/com/hasegawa/diapp/controllers/ListNewsController.kt#L71),
and when it gets a request to render news, all it does is update the data in the `RecyclerView.Adapter` and call `notifyDataSetChanged()`.

---

## Dependency Injection

The example above shows one simple presenter, the [ListNewsPresenter](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/presentation/src/main/java/com/hasegawa/diapp/presentation/presenters/ListNewsPresenter.kt).

It has one problem though, that list of parameters in its constructor. This is one problem with "dependency injection", where the objects require the instance of many others in order for it to function.

[Dagger2](http://google.github.io/dagger/) to the rescue \o/. Why? well, instead of manually entering all those parameters, we can [declare the presenter to be injected](https://github.com/AranHase/PassosDoImpeachment-app/blob/5513cd8e7249b92844dac1000ab781604cb50298/app/src/main/java/com/hasegawa/diapp/controllers/ListNewsController.kt#L38)
then [ask Dagger2 to inject it for us](https://github.com/AranHase/PassosDoImpeachment-app/blob/5513cd8e7249b92844dac1000ab781604cb50298/app/src/main/java/com/hasegawa/diapp/controllers/ListNewsController.kt#L55).
And that is it! \o/. Dagger2 just figured out how to build all the parameters of the `ListNewsPresenter`, then it instantiated a `ListNewsPresenter` for us, and put it nicelly in our parameter.
Isn't Dagger2 a sweet little.. amm thing??

Now, the question on how Dagger2 actually know what object to instantiate is more complex and will not be covered here, but it is mostly just about
[components](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/app/src/main/java/com/hasegawa/diapp/di/ActivityComponent.kt)
and [modules](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/app/src/main/java/com/hasegawa/diapp/di/ActivityModule.kt).


---


## Automated Testing

### domain module

Testing in the *domain* module is pretty straightforward because it only contains *use cases* as concrete classes. We can assume everything else is working perfectly.

Although, *use cases* depend on *repositories*, *rest services* and etc. That is where [Mockito](http://mockito.org/) comes into play.
Mockito is awesome, it allows us to quickly fake a repository ([for example](https://github.com/AranHase/PassosDoImpeachment-app/blob/03f95d8ce877c69636b6cc476bae956a5fc73524/domain/src/test/java/com/hasegawa/diapp/domain/GetNewsUseCaseTest.kt#L53) the `when` keyword is part of Mockito),
and focus on the functionality of the *use case* being tested.


### cloud module

The *cloud* module implements the REST client using Retrofit. This client will do HTTP requests. To properly test it, the [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver)
project is used to create a local mock server. MockWebServer lets us quickly test the behavior of our client with different responses and can even simulate bad connections.

Here is a [code example](https://github.com/AranHase/PassosDoImpeachment-app/blob/994c4a4d8d1bf64718c2069cdc3c4850e132fd36/cloud/src/test/java/com/hasegawa/diapp/cloud/RetrofitRestServiceTest.kt#L51).


### db module

The *db* module implements the repositories interfaces using Content Providers and SQLite, part of the Android Framework.
But, thanks to the [Robolectric](http://robolectric.org/) project we don't have to run these tests on an Android emulator or a real device \o/.
Robolectric will fake a Content Provider and SQLite database for us \o/.

Robolectric is pretty simple too, just run your tests on its environment, and use the functions as one would in Android.
Here is a [code example](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/db/src/test/java/com/hasegawa/diapp/db/ContentProviderNewsRepositoryTest.kt#L60).
**Note** how you can get a `ContentResolver` from the runtime environment :)

### presentation module

Tests for the *presentation* module were skipped in favor of tests in the *app* module.

### app module

The *app* module is where the application is built, so testing it is a bit less straighforward. First, the app will have full syncing capabilities.
It will try connect to the cloud and access the actual database on the device. Because content from the cloud is not predictable, we can't use it for testing.

This is where Dagger2 makes things a lot easier. We can create a Dagger2 component telling what kind of implementations we want use for the repositories/rest services interfaces.
For testing, we want to use an in-memory implementation, and insert into it what we want just for the tests.
[This is the component/module that does it](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/app/src/main/java/com/hasegawa/diapp/di/MemRepositoriesDI.kt).

In our tests we can just use that component instead of the usual one that uses *real* implementations:
[code](https://github.com/AranHase/PassosDoImpeachment-app/blob/da46750cc3186c15a67aee7ed5764615d9f230c8/app/src/androidTest/java/com/hasegawa/diapp/not_tests/BaseTest.kt#L36)

With the *mock* implementations injected into our app, we can then populate the fake database with a predictable content: [like this](https://github.com/AranHase/PassosDoImpeachment-app/blob/master/app/src/androidTest/java/com/hasegawa/diapp/not_tests/MemMockGen.kt).

The actual testing of the app is done with [Espresso](https://google.github.io/android-testing-support-library/docs/espresso/), a view hierarchy based testing framework.
Most of the time, we must first find a view in the hierarchy and then do some action on it, like a click or check if it is visible.
This is where a predictable content comes in handy, because we can then find the view using its text,
[like this](https://github.com/AranHase/PassosDoImpeachment-app/blob/da46750cc3186c15a67aee7ed5764615d9f230c8/app/src/androidTest/java/com/hasegawa/diapp/StepDetailScreenTest.kt#L60).
Because we have many views with id `R.id.step_title_tv`, we can differentiate between them using its text content.

In short, the *app* module testing is all about mocking the data, and test a bunch of actions and check if the screen is showing what we expect it to show :)


---


## Where are all the comments?

After applying this architecture, and making sure all the rules are followed and everything named properly, I found almost no reason to add comments in code.


---

## Running

To get things started: 

 1. Get a GCM configuration file and place it in the `app` folder.
    1. GCM file from [here](https://developers.google.com/cloud-messaging/android/start).
 2. Open the project in Android Studio and just run it.
 3. By default, the app will fetch data from the internet on first run (or when data is outdated). You can easily change the REST config in the file `RestConfig.kt` to use your local server.
 

---

## License

Passos do Impeachment (app) is published under the Apache 2.0 license.
