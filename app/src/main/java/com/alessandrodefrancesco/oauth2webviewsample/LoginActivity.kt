package com.alessandrodefrancesco.oauth2webviewsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.alessandrodefrancesco.oauth2webviewsample.MyApplication.Companion.accessTokenManager
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        MyApplication.accessTokenManager.setUpWebView(webView,
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
