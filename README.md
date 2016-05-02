# Passos do Impeachment (Android app)

> This README was written before refactoring the app to use the "Clean-way Android Architecture". I'm working on a new one to document the architecture and details of this new improved version :)

[![Play Link](http://steverichey.github.io/google-play-badge-svg/img/en_get.svg)](https://play.google.com/store/apps/details?id=com.hasegawa.diapp)

A *full* **demo** Android app showcasing data syncing with a RESTful server. Android [SyncAdapter](http://developer.android.com/training/sync-adapters/creating-sync-adapter.html) is used to schedule the appropriate time to sync the data, a process done with Retrofit. StorIO caches the data for offline usage. To keep devices up-to-date, the server issues GCM messages to the client when things changes. Kotlin and RxJava makes everything beautiful.

In short, the following awesome projects were used:

  * [Kotlin](https://kotlinlang.org/)
  * [RxJava](https://github.com/ReactiveX/RxJava)
  * [StorIO](https://github.com/pushtorefresh/storio)
  * [Retrofit](http://square.github.io/retrofit/)
  * [Google Cloud Messaging (GCM)](https://developers.google.com/cloud-messaging/)
  * [Espresso](https://google.github.io/android-testing-support-library/docs/espresso/)

The app has an accompanying **server** to sync the data with. Check it here: [PassosDoImpeachment-server](https://github.com/AranHase/PassosDoImpeachment-server)

## Running

To get things started: 

 1. Get a GCM configuration file and place it in the `app` folder.
    1. GCM file from [here](https://developers.google.com/cloud-messaging/android/start).
 2. Open the project in Android Studio and just run it.
 3. By default, the app will fetch data from the internet on first run (or when data is outdated). You can easily change the REST config in the file `RestConfig.kt` to use your local server.


## License

Passos do Impeachment (app) is published under the Apache 2.0 license.
