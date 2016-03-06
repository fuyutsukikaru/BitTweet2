package org.chromatiqa.bittweet2.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.util.*

class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

    final val fragList: ArrayList<Fragment> = arrayListOf()
    final val titleList: ArrayList<String> = arrayListOf()

    override fun getItem(position: Int): Fragment = fragList[position]

    override fun getCount(): Int = fragList.size
    
    fun addFrag(fragment: Fragment, title: String) {
        fragList.add(fragment)
        titleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence = titleList[position]
}
