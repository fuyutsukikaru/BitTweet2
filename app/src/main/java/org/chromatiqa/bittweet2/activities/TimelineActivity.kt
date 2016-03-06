package org.chromatiqa.bittweet2.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.koushikdutta.async.future.Future
import com.koushikdutta.ion.Ion
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.models.User
import kotlinx.android.synthetic.main.activity_timeline.*
import org.chromatiqa.bittweet2.R
import org.chromatiqa.bittweet2.adapters.ViewPagerAdapter
import org.chromatiqa.bittweet2.fragments.HomeFragment
import org.chromatiqa.bittweet2.fragments.MentionsFragment
import org.chromatiqa.bittweet2.utils.RoundedTransformation

class TimelineActivity : AppCompatActivity() {

    var adapter: ViewPagerAdapter? = null
    var drawerLayout : DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val users = getSharedPreferences("users", MODE_PRIVATE)
        var username = users.getString("current_user", null)
        if (username == null) {
            val intent = Intent(this@TimelineActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val prefs = getSharedPreferences(username, MODE_PRIVATE)
        val userId = prefs.getLong("userId", 0)

        setContentView(R.layout.activity_timeline)

        val toolbar : Toolbar = this.toolbar
        setSupportActionBar(toolbar)

        val ab : ActionBar? = supportActionBar
        //ab!!.setHomeAsUpIndicator(R.mipmap.ic_menu)
        ab!!.setDisplayHomeAsUpEnabled(true)

        val api = TwitterCore.getInstance().apiClient
        api.accountService.verifyCredentials(true, false, object: Callback<User>() {
            override fun success(result: Result<User>) {
                val bitmap = Ion.with(this@TimelineActivity)
                        .load(result.data.profileImageUrlHttps)
                        .asBitmap().get()
                val rs = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
                val cb = RoundedTransformation(250.0F, 0.0F, true, true, true, true).transform(rs)
                toolbar.navigationIcon = BitmapDrawable(resources, cb)
                rs.recycle()
                bitmap.recycle()
            }

            override fun failure(e: TwitterException) {
                e.printStackTrace()
            }
        })

        val viewPager = this.view_pager
        setupViewPager(viewPager)

        drawerLayout = this.drawer_layout
        val navView = this.nav_view
        setupDrawerContent(navView, viewPager)

    }

    private fun setupViewPager(viewPager: ViewPager) {
        adapter = ViewPagerAdapter(supportFragmentManager)
        adapter!!.addFrag(HomeFragment(), "Home")
        adapter!!.addFrag(MentionsFragment(), "Mentions")
        viewPager.adapter = adapter
    }

    private fun setupDrawerContent(navView: NavigationView, viewPager: ViewPager) {
        val header = navView.inflateHeaderView(R.layout.drawer_header) as LinearLayout
        navView.setNavigationItemSelectedListener {
            menuItem -> when(menuItem.itemId) {
                R.id.nav_home -> {
                    menuItem.isChecked = true
                    viewPager.setCurrentItem(0, true)
                }
                R.id.nav_mentions -> {
                    menuItem.isChecked = true
                    viewPager.setCurrentItem(1, true)
                }
                R.id.settings -> {

                }
                R.id.logout -> {

                }
            }
            drawerLayout!!.closeDrawers()
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
