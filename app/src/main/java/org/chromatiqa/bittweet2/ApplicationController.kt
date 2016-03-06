package org.chromatiqa.bittweet2

import android.app.Application
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import io.fabric.sdk.android.Fabric

class ApplicationController : Application() {

    val TWITTER_KEY : String = "1N1fsuVO2VSYgzu690kxA";
    val TWITTER_SECRET : String = "QFLtPeggR6UOPWF3cDHcNmLaZMJ8aTf7ewIyPVNfg";

    companion object {
        var singleton: ApplicationController? = null
            get(): ApplicationController? = singleton
    }

    override fun onCreate() {
        super.onCreate()
        singleton = this

        val authConfig : TwitterAuthConfig = TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, Twitter(authConfig))
    }
}
