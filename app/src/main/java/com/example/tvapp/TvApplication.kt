package com.example.tvapp

import android.app.Application
import com.amplifyframework.core.Amplify
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin

class TvApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        RefreshWorker.schedule(this)
    }
}
