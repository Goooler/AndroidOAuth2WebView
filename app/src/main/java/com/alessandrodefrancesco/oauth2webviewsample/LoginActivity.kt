package com.alessandrodefrancesco.oauth2webviewsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.alessandrodefrancesco.oauth2webviewsample.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyApplication.accessTokenManager.setUpWebView(binding.webView,
            loginFail = {
                Log.d("Login", "Failure")
                Toast.makeText(this, "LOGIN FAILURE", Toast.LENGTH_SHORT).show()
                finish()
            },
            loginSuccess = {
                Log.d("Login", "Success")
                Toast.makeText(this, "LOGGED", Toast.LENGTH_SHORT).show()

                startActivity(MainActivity.newIntent(this))
                finish()
            }
        )
    }
}
