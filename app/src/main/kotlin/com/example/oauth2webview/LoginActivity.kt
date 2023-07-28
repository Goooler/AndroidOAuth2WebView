package com.example.oauth2webview

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.oauth2webview.databinding.ActivityLoginBinding
import io.goooler.oauth2webview.OAuth2AccessToken
import io.goooler.oauth2webview.OAuth2Exception
import io.goooler.oauth2webview.OAuth2StateListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyApplication.accessTokenManager.setUpWebView(
            binding.webView,
            object : OAuth2StateListener {
                override fun onSuccess(token: OAuth2AccessToken) {
                    binding.flLoading.isVisible = false

                    Toast.makeText(this@LoginActivity, "LOGGED", Toast.LENGTH_SHORT).show()
                    Log.d("LoginActivity", token.toString())

                    startActivity(MainActivity.newIntent(this@LoginActivity))
                    finish()
                }

                override fun onFailure(e: OAuth2Exception) {
                    binding.flLoading.isVisible = false

                    val message = if (e is OAuth2Exception.UserCancelException) e.message else "LOGIN FAILURE"
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()

                    finish()
                }

                override fun onLoading() {
                    binding.flLoading.isVisible = true
                }
            },
        )
    }
}
