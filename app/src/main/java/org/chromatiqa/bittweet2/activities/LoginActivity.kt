package org.chromatiqa.bittweet2.activities

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import kotlinx.android.synthetic.main.activity_login.*
import org.chromatiqa.bittweet2.R


class LoginActivity : AppCompatActivity() {

    var loginButton : TwitterLoginButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = this.twitter_login_button;
        loginButton!!.callback = object: Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                val session = result.data
                val userId = session.userId
                val username = session.userName
                val authToken = session.authToken

                val users = getSharedPreferences("users", MODE_PRIVATE)
                val userSet = users.getStringSet("logged_in", null)
                users.edit().putString("current_user", username).apply()
                if (userSet == null) {
                    val set = setOf(username)
                    users.edit().putStringSet("logged_in", set).apply()
                } else {
                    userSet.add(username)
                    users.edit().putStringSet("logged_in", userSet).apply()
                }

                val prefs = getSharedPreferences(username, MODE_PRIVATE)
                prefs.edit().putString("token", authToken.token).apply()
                prefs.edit().putString("secret", authToken.secret).apply()
                prefs.edit().putLong("userId", userId).apply()

                val intent : Intent = Intent(this@LoginActivity, TimelineActivity::class.java)
                startActivity(intent)
                finish()
            }
            override fun failure(exception: TwitterException) {
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result to the login button.
        loginButton!!.onActivityResult(requestCode, resultCode, data)
    }
}
