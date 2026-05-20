package com.recapped.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class anotada con @HiltAndroidApp.
 * Habilita la inyección de dependencias en toda la app.
 */
@HiltAndroidApp
class RecappedApp : Application()
