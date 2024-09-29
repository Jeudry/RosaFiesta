package com.example.rosafiesta

import android.app.Application
import com.example.auth.data.di.authDataModule
import com.example.auth.presentation.di.authViewModelModule
import com.example.core.data.di.coreDataModule
import com.example.rosafiesta.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber

class RFApp : Application() {
  
  val applicationScope = CoroutineScope(SupervisorJob())
  
  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
    
    startKoin {
      androidLogger()
      androidContext(this@RFApp)
      modules(
        authDataModule,
        authViewModelModule,
        appModule,
        coreDataModule
      )
    }
  }
}