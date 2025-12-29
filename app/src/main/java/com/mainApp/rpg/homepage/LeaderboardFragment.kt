package com.mainApp.rpg.homepage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mainApp.rpg.R
import com.mainApp.rpg.databinding.FragmentLeaderboardBinding
import com.mainApp.rpg.homepage.inventory.InventoryManager
import com.mainApp.rpg.homepage.inventory.Item
import com.mainApp.rpg.requests.getUserToken
import com.mainApp.rpg.requests.makeGetRequest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.LinkedList

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val leaderboardList: LinkedList<Item> = LinkedList<Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView =  binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = InventoryManager(leaderboardList, ::doNothing) { holder, item ->
            holder.imageButton.setImageResource(item.imageResId)
            holder.imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.text.text = item.text
        }
        loadLeaderboard()
        return root
    }

    fun doNothing(ind: Int, item: Item) {}

    fun loadLeaderboard() {
        lifecycleScope.launch {
            val token = getUserToken()
            if (token == null) return@launch

            val res = makeGetRequest("https://bikinggamebackend.vercel.app/api/leaderboard", token)
            if (!res.has("data")) return@launch

            val leaderboard = res.get("data") as JSONObject
            leaderboardList.clear()

            val getImage: (Int) -> Int = { ind ->
                when (ind) {
                    0 -> R.drawable.numberone
                    1 -> R.drawable.numbertwo
                    2 -> R.drawable.numberthree
                    else -> R.drawable.lessthanthree
                }
            }


            for (i in 0 until 100) {
                if (!leaderboard.has(i.toString())) break
                val entry = leaderboard.get(i.toString()) as JSONObject
                val item = Item(getImage(i), "${entry.get("username")}: ${entry.get("deepestRoom")}")

                leaderboardList.add(item)
            }

            requireActivity().runOnUiThread {
                binding.recyclerView.adapter = InventoryManager(leaderboardList, ::doNothing) { holder, item ->
                    holder.imageButton.setImageResource(item.imageResId)
                    holder.imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
                    holder.text.text = item.text
                }
            }
        }
    }
}