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

        // Initialize Twitter login button
        loginButton = this.twitter_login_button;
        loginButton!!.callback = object: Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                val session = result.data
                val userId = session.userId
                val username = session.userName
                val authToken = session.authToken

                // Store new user and set the current user to new user
                val users = getSharedPreferences("users", MODE_PRIVATE)
                val userSet = users.getStringSet("logged_in", null)
                users.edit().putString("current_user", username).apply()
                // Add user to set of existing users. If none, create new set
                if (userSet == null) {
                    val set = setOf(username)
                    users.edit().putStringSet("logged_in", set).apply()
                } else {
                    userSet.add(username)
                    users.edit().putStringSet("logged_in", userSet).apply()
                }

                // Store user's token and secret
                val prefs = getSharedPreferences(username, MODE_PRIVATE)
                prefs.edit().putString("token", authToken.token).apply()
                prefs.edit().putString("secret", authToken.secret).apply()
                prefs.edit().putLong("userId", userId).apply()

                // Start timeline activity
                val intent : Intent = Intent(this@LoginActivity, TimelineActivity::class.java)
                startActivity(intent)
                finish()
            }
            override fun failure(exception: TwitterException) {
                // On failure to authenticate, close app
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
