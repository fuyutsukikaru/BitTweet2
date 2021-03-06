package org.chromatiqa.bittweet2.adapters

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.Toast
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.LinkBuilder
import com.koushikdutta.ion.Ion
import com.tr4android.recyclerviewslideitem.SwipeAdapter
import com.tr4android.recyclerviewslideitem.SwipeConfiguration
import com.twitter.sdk.android.core.models.*
import kotlinx.android.synthetic.main.activity_timeline.*
import org.chromatiqa.bittweet2.R
import java.util.*
import kotlinx.android.synthetic.main.row_tweet.view.*
import org.chromatiqa.bittweet2.activities.TimelineActivity
import org.chromatiqa.bittweet2.fragments.MentionsFragment
import org.chromatiqa.bittweet2.utils.RoundedTransformation
import java.text.SimpleDateFormat
import java.util.regex.Pattern

class TweetsAdapter(val context: Context, val userId: Long, val frag: Fragment) : SwipeAdapter() {

    val tweets = arrayListOf<Tweet>()

    // Setup static viewholder object
    companion object {
        class TweetViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val avatarImage = v.avatar
            val username = v.username
            val displayname = v.displayname
            val time = v.time
            val tweet = v.tweet
            val accent = v.accent_container
            val rtBy = v.retweeted_by
            val previewContainer = v.preview_container
            val prevList = arrayListOf(v.media_expansion, v.preview1, v.preview2, v.preview3, v.preview4)
            val tweetContainer = v.tweet_text_container
            val row = v.row_item
        }
    }

    // Load tweets into adapter
    fun putTweets(list: List<Tweet>) {
        tweets.addAll(0, list)
        // If new tweets were added, keep scroll position
        if (list.size > 0) {
            notifyItemRangeInserted(0, list.size)
        } else {
            notifyDataSetChanged()
        }
    }

    override fun onCreateSwipeViewHolder(parent: ViewGroup, i: Int): TweetViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_tweet, parent, true)
        return TweetViewHolder(v)
    }

    override fun onBindSwipeViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Cast holder to TweetViewHolder and retrieve tweet at position
        val h = holder as TweetViewHolder
        var tweet = tweets[position]

        // Defaults
        h.prevList.forEach {
            it.visibility = View.GONE
        }
        h.accent.visibility = View.GONE
        h.rtBy.visibility = View.GONE

        // Setup retweetedBy string if retweeted
        var retweetedBy: String? = null
        if (tweet.retweetCount > 0) {
            retweetedBy = if (tweet.retweeted) {
                context.resources.getString(R.string.you)
            } else {
                "@" + tweet.user.screenName
            }
            tweet = tweet.retweetedStatus ?: tweet
        }

        // Setup highlight accent and tweet background color
        if (tweet.favorited) {
            h.accent.visibility = View.VISIBLE
            h.accent.setBackgroundColor(ContextCompat.getColor(context, R.color.favorite_accent))
        } else if (tweet.retweetCount > 0 || tweet.retweeted) {
            h.rtBy.visibility = View.VISIBLE
            h.accent.visibility = View.VISIBLE
            h.rtBy.text = String.format(context.getString(R.string.retweeted_by), retweetedBy!!)
        }

        if (tweet.inReplyToUserId == userId && frag !is MentionsFragment) {
            h.accent.visibility = View.VISIBLE
            h.accent.setBackgroundColor(ContextCompat.getColor(context, R.color.reply_accent))
            h.row.setBackgroundColor(ContextCompat.getColor(context, R.color.reply_background))
        }

        h.username.text = "@" + tweet.user.screenName
        h.displayname.text = tweet.user.name

        // Setup date display based on when the tweet was made
        val date = SimpleDateFormat("E MMM dd K:mm:ss Z yyyy").parse(tweet.createdAt)
        val time = Date().time - date.time
        h.time.text = if (time / (1000 * 60) < 1) {
            (time / 1000).toString() + " seconds ago"
        } else if (time / (1000 * 60 * 60) < 1) {
            (time / (1000 * 60)).toString() + " minutes ago"
        } else if (time / (1000 * 60 * 60 * 24) < 1) {
            (time / (1000 * 60 * 60)).toString() + " hours ago"
        } else if (time / (1000 * 60 * 60 * 24) < 30) {
            (time / (1000 * 60 * 60 * 24)).toString() + " days ago"
        } else {
            SimpleDateFormat("MMM dd, yyyy").format(date)
        }

        // Load profile image into tweet
        Ion.with(h.avatarImage)
            .resize(150, 150)
            .transform(RoundedTransformation(250.0F, 0.0F, true, true, true, true))
            .load(tweet.user.profileImageUrl)

        // Entities
        val media = tweet.extendedEtities?.media?.map {
            it -> it.mediaUrlHttps
        }

        // Regex patterns for matching urls with images
        // TODO: Add more sources (twitpic, minus), YouTube, Vines, and GIFs
        val imageID = Regex("^https?://(?:[a-z\\-]+\\.)+[a-z]{2,6}(?:/[^/#?]+)+\\.(?:jpe?g|gif|png)$")
        val pixivID = Pattern.compile("^http://www\\.pixiv\\.net/(member_illust|index)\\.php\\?(?=.*mode=(medium|big))(?=.*illust_id=([0-9]+)).*$", Pattern.CASE_INSENSITIVE)
        // Get the full url of links that match the specified regex
        val urls = tweet.extendedEtities?.urls?.map {
            it.expandedUrl
        }?.filter {
            pixivID.matcher(it).matches() || it.matches(imageID)
        }?.map {
            val pixivMatcher = pixivID.matcher(it)
            if (pixivMatcher.matches()) {
                "http://embed.pixiv.net/decorate.ph[?illust_id=" + pixivMatcher.group(3)
            } else {
                it
            }
        }

        // Replace links in tweet with expanded media and url links
        var replaced_tweet = tweet.text
        tweet.extendedEtities?.media?.forEach {
            replaced_tweet = replaced_tweet.replace(it.url, it.displayUrl)
        }
        tweet.extendedEtities?.urls?.forEach {
            replaced_tweet = replaced_tweet.replace(it.url, it.displayUrl)
        }
        h.tweet.text = replaced_tweet

        // Set links to be clickable as well as applying styles on them
        // TODO: Make this work or find new library
        if (tweet.extendedEtities != null) {
            val linkUrls = if (tweet.extendedEtities.urls != null) {
                getLinks(tweet.extendedEtities.urls).filterNotNull()
            } else {
                listOf()
            }
            val linkMentions = if (tweet.extendedEtities.userMentions != null) {
                getLinks(tweet.extendedEtities.userMentions).filterNotNull()
            } else {
                listOf()
            }
            val linkHashtags = if (tweet.extendedEtities.hashtags != null) {
                getLinks(tweet.extendedEtities.hashtags).filterNotNull()
            } else {
                listOf()
            }
            val linkFinal = linkUrls + linkMentions + linkHashtags
            if (linkFinal.size > 0) {
                LinkBuilder.on(h.tweet)
                        .addLinks(linkFinal)
                        .build()
            }
        }

        // Setup image previews
        if (media != null && media.size > 0) {
            listToView(media, h)
        } else if (urls != null && urls.size > 0) {
            listToView(urls, h)
        }
    }

    // Take in a list of entities and transform to a list of Links
    private fun <A> getLinks(links: List<A>) : List<Link?> = links?.map {
        when(it) {
            is UrlEntity -> Link(it.displayUrl)
                    .setTextColor(R.color.unpressed_link_color)
                    .setUnderlined(false)
                    .setBold(true)
                    .setOnClickListener {

                    }.setOnLongClickListener {

                    }
            is MentionEntity -> Link(Pattern.compile("@" + it.screenName))
                    .setTextColor(R.color.unpressed_link_color)
                    .setUnderlined(false)
                    .setBold(true)
                    .setOnClickListener {

                    }.setOnLongClickListener {

                    }
            is HashtagEntity -> Link(Pattern.compile("#" + it.text))
                    .setTextColor(R.color.unpressed_link_color)
                    .setUnderlined(false)
                    .setBold(true)
                    .setOnClickListener {

                    }.setOnLongClickListener {

                    }
            else -> {
                null
            }
        }
    }

    // Set the image asynchronously into the imageview with transformations
    private fun setImage(v: ImageView, width: Int, height: Int, tl: Boolean, tr: Boolean, bl: Boolean, br: Boolean, url: String) {
        Ion.with(v)
            .error(R.mipmap.ic_launcher)
            .resize(width, height)
            .centerCrop()
            .transform(RoundedTransformation(20.0F, 0.0F, tl, tr, bl, br))
            .load(url)
    }

    // Load image previews with proper transformation and setup onclick method
    private fun listToView(list: List<String>, h: TweetViewHolder) {
        h.previewContainer.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
            // Necessary to measure the size of the imageview before it is drawn
            override fun onPreDraw(): Boolean {
                h.previewContainer.viewTreeObserver.removeOnPreDrawListener(this)
                val width = h.previewContainer.width
                // Match to number of urls (up to 4) and load previews
                // TODO: Make this cleaner if possible
                when(list.size) {
                    1 -> {
                        setImage(h.prevList[0], width, width * 5/8, true, true, true, true, list[0])
                        h.prevList[0].visibility = View.VISIBLE
                    }
                    2 -> {
                        setImage(h.prevList[1], width/2, width * 5/8, true, false, true, false, list[0])
                        setImage(h.prevList[2], width/2, width * 5/8, false, true, false, true, list[1])
                        h.prevList.forEachIndexed {
                            index, it -> if (index > 0 && index < 3) it.visibility = View.VISIBLE
                        }
                    }
                    3 -> {
                        setImage(h.prevList[1], width/2, width/2 * 5/8, true, false, false, false, list[0])
                        setImage(h.prevList[2], width/2, width * 5/8, false, true, false, true, list[1])
                        setImage(h.prevList[3], width/2, width/2 * 5/8, false, false, true, false, list[2])
                        h.prevList.forEachIndexed {
                            index, it -> if (index > 0 && index < 4) it.visibility = View.VISIBLE
                        }
                    }
                    else -> {
                        if (list.size >= 4) {
                            setImage(h.prevList[1], width/2, width/2 * 5/8, true, false, false, false, list[0])
                            setImage(h.prevList[2], width/2, width/2 * 5/8, false, true, false, false, list[1])
                            setImage(h.prevList[3], width/2, width/2 * 5/8, false, false, true, false, list[2])
                            setImage(h.prevList[4], width/2, width/2 * 5/8, false, false, false, true, list[3])
                            h.prevList.forEachIndexed {
                                index, it -> if (index > 0) it.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                // Setup onclicklistener for each image preview
                h.prevList.forEachIndexed {
                    index, it -> it.setOnClickListener {
                        //val bitmap = Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
                        //val animate = ActivityOptions.makeThumbnailScaleUpAnimation(it, bitmap, 0, 0)
                        //val intent : Intent = Intent(context, TimelineActivity::class.java)
                        //intent.putExtra("media", list as Array<String>)
                        //intent.putExtra("position", index)
                        //context.startActivity(intent, animate.toBundle())
                    }
                }
                return true
            }
        })
    }

    // Setup the configurations for the swipe recyclerview, which allows for actions on swipe
    // TODO: Add in icons for each swipe behavior and mess with the swipe behavior responses
    override fun onCreateSwipeConfiguration(context: Context, position: Int): SwipeConfiguration =
            SwipeConfiguration.Builder(context)
                    .setRightBackgroundColorResource(R.color.retweet_accent)
                    .setLeftBackgroundColorResource(R.color.favorite_accent)
                    .setRightDrawableResource(R.mipmap.ic_reply_black_24dp)
                    .setLeftDrawableResource(R.mipmap.ic_star_border_black_24dp)
                    .setRightSwipeBehaviour(SwipeConfiguration.SwipeBehaviour.RESTRICTED_SWIPE)
                    .setLeftSwipeBehaviour(SwipeConfiguration.SwipeBehaviour.RESTRICTED_SWIPE)
                    .build()

    // Setup actions on the swipe direction
    // TODO: Add in functionality for swipe to Reply and swipe to Like
    override fun onSwipe(position: Int, direction: Int) {
        if (direction == SWIPE_LEFT) {
            System.err.println("LEFT")
            Snackbar.make(frag.activity.view_pager, fav_responses[Math.floor(Math.random() * fav_responses.size).toInt()], Snackbar.LENGTH_LONG).show()
        } else {
            System.err.println("RIGHT")
            Snackbar.make(frag.activity.view_pager, "This feature has been deemed illegal by the tweet police. Thanks Benson.", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int = tweets.size

    // Get the id of the tweet at the top of the list in order to load new tweets starting from top
    fun getTopId(): Long? = if (tweets.size == 0) null else tweets.first().id

    val fav_responses = listOf<String>(
            "You though you had stars, but nope.",
            "Stars? Aikatsu Stars.",
            "All your base belong to us.",
            "You wish you could favorite all these dank memes, don't you.",
            "Thank Benson for the stars."
    )
}
