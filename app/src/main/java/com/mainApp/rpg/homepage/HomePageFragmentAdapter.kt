package com.mainApp.rpg.homepage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mainApp.rpg.homepage.inventory.InventoryFragment

class HomePageFragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InventoryFragment()
            1 -> HomePageFragment()
            else -> LeaderboardFragment()
        }
    }
}