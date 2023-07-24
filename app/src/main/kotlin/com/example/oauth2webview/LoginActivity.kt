package com.example.oauth2webview

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.oauth2webview.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyApplication.accessTokenManager.setUpWebView(binding.webView) {
            it.onSuccess {
                Log.d("Login", "Success")
                Toast.makeText(this, "LOGGED", Toast.LENGTH_SHORT).show()

                startActivity(MainActivity.newIntent(this))
                finish()
            }
            it.onFailure {
                Log.d("Login", "Failure")
                Toast.makeText(this, "LOGIN FAILURE", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
