# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\NVPACK\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:


# From ButterKnife site
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Retrofit
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8

# Kotlin
-dontwarn kotlin.**
# Allows to use some kotlin during tests
-keep class kotlin.jvm.internal.** { *; }
-keep class kotlin.collections.** { *; }
-keep class kotlin.Unit { *; }


# RxJava
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}


# From Retrofit site
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions


# Conductor::Controller must have a default constructor or the bundle one.
-keep class * extends com.bluelinelabs.conductor.Controller
-keepclassmembers class * extends com.bluelinelabs.conductor.Controller {
    public <init>();
    public <init>(android.os.Bundle);
}

# Dagger2
-keep class javax.inject.* { *; }

# Prevent issues with Kotlin fields and lambdas
-dontwarn com.hasegawa.diapp.presentation.presenters.**
-dontwarn com.hasegawa.diapp.domain.usecases.**

# Those classes are kept because they are used in mocks
-keep class * extends com.hasegawa.diapp.presentation.presenters.Presenter { *; }
-keep class * extends com.hasegawa.diapp.domain.usecases.UseCase { *; }
-keep class com.hasegawa.diapp.domain.restservices.responses.StepResponse { *; }
-keep class com.hasegawa.diapp.domain.restservices.responses.StepLinkResponse { *; }
-keep class com.hasegawa.diapp.domain.restservices.responses.NewsResponse { *; }
-keep class com.hasegawa.diapp.domain.entities.GCMRegistrationEntity { *; }
-keep class com.hasegawa.diapp.domain.entities.StepEntity { *; }
-keep class com.hasegawa.diapp.domain.entities.StepLinkEntity { *; }
-keep class com.hasegawa.diapp.domain.entities.NewsEntity { *; }
-keep class com.hasegawa.diapp.domain.entities.SyncEntity { *; }

# Methods that are used in the tests
-keep class com.hasegawa.diapp.db.repositories.mocks.mem.MemStepsRepository { *; }
-keep class com.hasegawa.diapp.db.repositories.mocks.mem.MemSyncsRepository { *; }
-keep class com.hasegawa.diapp.db.repositories.mocks.mem.MemNewsRepository { *; }
-keep class com.hasegawa.diapp.db.repositories.mocks.mem.MemStepsRepository$Companion { *; }
-keep class com.hasegawa.diapp.db.repositories.mocks.mem.MemSyncsRepository$Companion { *; }
-keep class com.hasegawa.diapp.db.repositories.mocks.mem.MemNewsRepository$Companion { *; }
-keep class com.hasegawa.diapp.DiApp { *; }
-keep class com.hasegawa.diapp.DiApp$Companion { *; }
-keep class com.hasegawa.diapp.di.DaggerAppMemComponent { *; }
-keep class com.hasegawa.diapp.di.DaggerAppMemComponent$Builder { *; }
-keep class android.support.v4.widget.DrawerLayout { *; }
-keep class android.support.v4.widget.DrawerLayout$DrawerListener { *; }
-keep class android.support.v7.widget.RecyclerView { *; }
-keep class kotlin.Metadata { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
