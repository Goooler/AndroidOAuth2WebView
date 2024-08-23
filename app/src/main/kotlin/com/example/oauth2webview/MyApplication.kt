package com.example.oauth2webview

import android.app.Application
import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.goooler.oauth2webview.OAuth2AccessTokenManager
import io.goooler.oauth2webview.OAuth2AccessTokenStorageSharedPreferences
import okhttp3.OkHttpClient

class MyApplication : Application() {

    companion object {
        lateinit var accessTokenManager: OAuth2AccessTokenManager
    }

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        val storageSharedPreferences = OAuth2AccessTokenStorageSharedPreferences(sharedPreferences)
        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(ChuckerInterceptor(this))
            .build()

        accessTokenManager = OAuth2AccessTokenManager(
            storage = storageSharedPreferences,
            // https://github.com/thundernest/k-9/blob/00148f1a9939c5d2aa82013007a5386f0b03cab3/app/k9mail/src/main/java/com/fsck/k9/auth/AppOAuthConfigurationFactory.kt#L53-L54
            authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
            tokenEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/token",
            // https://github.com/thundernest/k-9/blob/c781e7032d72e8dc457c154bbea6473713861886/app/k9mail/build.gradle.kts#L143
            clientId = "e647013a-ada4-4114-b419-e43d250f99c5",
            clientSecret = null,
            // https://github.com/thundernest/k-9/blob/c781e7032d72e8dc457c154bbea6473713861886/app/k9mail/build.gradle.kts#L147
            redirectUri = "msauth://com.fsck.k9.debug/VZF2DYuLYAu4TurFd6usQB2JPts%3D",
            // https://github.com/thundernest/k-9/blob/00148f1a9939c5d2aa82013007a5386f0b03cab3/app/k9mail/src/main/java/com/fsck/k9/auth/AppOAuthConfigurationFactory.kt#L48-L52
            scope = listOf(
                "https://outlook.office.com/IMAP.AccessAsUser.All",
                "https://outlook.office.com/SMTP.Send",
                "offline_access",
            ).joinToString(" "),
            client = okHttpClient,
        ).apply {
            prompt = "select_account"
        }
    }
}
