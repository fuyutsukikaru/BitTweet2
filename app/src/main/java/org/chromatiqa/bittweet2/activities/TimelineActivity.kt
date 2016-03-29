package org.chromatiqa.bittweet2.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
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
import com.roughike.bottombar.BottomBar
import com.roughike.bottombar.BottomBarTab
import com.roughike.bottombar.OnMenuTabClickListener
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.models.User
import kotlinx.android.synthetic.main.activity_timeline.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import org.chromatiqa.bittweet2.R
import org.chromatiqa.bittweet2.adapters.ViewPagerAdapter
import org.chromatiqa.bittweet2.fragments.FavsFragment
import org.chromatiqa.bittweet2.fragments.HomeFragment
import org.chromatiqa.bittweet2.fragments.MentionsFragment
import org.chromatiqa.bittweet2.utils.ImageUtils
import org.chromatiqa.bittweet2.utils.RoundedTransformation

class TimelineActivity : AppCompatActivity() {

    var adapter: ViewPagerAdapter? = null
    var drawerLayout : DrawerLayout? = null
    var bottomBar : BottomBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the current active user. If none, start login activity
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

        // Setup toolbar and use as actionbar
        val toolbar : Toolbar = this.toolbar
        setSupportActionBar(toolbar)

        val ab : ActionBar? = supportActionBar
        //ab!!.setHomeAsUpIndicator(R.mipmap.ic_menu)
        ab!!.setDisplayHomeAsUpEnabled(true)

        // Load user profile picture in the action bar
        val api = TwitterCore.getInstance().apiClient
        api.accountService.verifyCredentials(true, false, object: Callback<User>() {
            override fun success(result: Result<User>) {
                // Get bitmap from user profile and transform to circular
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
                // TODO: On failure, put default image
                e.printStackTrace()
            }
        })

        // Setup viewpager
        val viewPager = this.view_pager
        setupViewPager(viewPager)

        // Setup navigation drawer
        drawerLayout = this.drawer_layout
        val navView = this.nav_view
        setupDrawerContent(navView, viewPager)

        bottomBar = BottomBar.attach(this, savedInstanceState)
        bottomBar!!.setItemsFromMenu(R.menu.bottombar, object: OnMenuTabClickListener {
            override fun onMenuTabSelected(menuItemId: Int) {
                if (menuItemId == R.id.nav_home) {
                    viewPager.setCurrentItem(0, false)
                } else if (menuItemId == R.id.nav_mentions) {
                    viewPager.setCurrentItem(1, false)
                } else if (menuItemId == R.id.nav_favs) {
                    viewPager.setCurrentItem(2, false)
                }
            }

            override fun onMenuTabReSelected(menuItemId: Int) {
                if (menuItemId == R.id.nav_home) {
                    viewPager.setCurrentItem(0, false)
                } else if (menuItemId == R.id.nav_mentions) {
                    viewPager.setCurrentItem(1, false)
                } else if (menuItemId == R.id.nav_favs) {
                    viewPager.setCurrentItem(2, false)
                }
            }
        })

    }

    // Initializes viewpager with an adapter and loads fragments into it
    private fun setupViewPager(viewPager: ViewPager) {
        adapter = ViewPagerAdapter(supportFragmentManager)
        adapter!!.addFrag(HomeFragment(), "Home")
        adapter!!.addFrag(MentionsFragment(), "Mentions")
        adapter!!.addFrag(FavsFragment(), "Favs")
        viewPager.adapter = adapter
    }

    // Initialize nav drawer, including header and menu items
    private fun setupDrawerContent(navView: NavigationView, viewPager: ViewPager) {
        val header = navView.inflateHeaderView(R.layout.drawer_header) as LinearLayout
        val api = TwitterCore.getInstance().apiClient
        api.accountService.verifyCredentials(true, false, object: Callback<User>() {
            override fun success(result: Result<User>) {
                // Get bitmap from user profile and transform to circular
                val bitmap = Ion.with(this@TimelineActivity)
                        .load(result.data.profileBannerUrl)
                        .asBitmap().get()
                val rs = ImageUtils.scaleCenterCrop(bitmap, header.width, header.height)
                header.background = BitmapDrawable(resources, rs)
                bitmap.recycle()
            }

            override fun failure(e: TwitterException) {
                // TODO: On failure, put default image
                e.printStackTrace()
            }
        })
        navView.setNavigationItemSelectedListener {
            menuItem -> when(menuItem.itemId) {
                R.id.settings -> {
                    Snackbar.make(viewPager, "Coming to a BitTweet near you (read 2034).", Snackbar.LENGTH_LONG).show()
                }
                R.id.logout -> {
                    Snackbar.make(viewPager, "You can never leave...", Snackbar.LENGTH_LONG).show()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        bottomBar?.onSaveInstanceState(outState)
    }
}
