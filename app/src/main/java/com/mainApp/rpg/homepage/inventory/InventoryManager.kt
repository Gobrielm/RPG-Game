package com.mainApp.rpg.homepage.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mainApp.rpg.R

data class Item(val imageResId: Int, val text: String)

data class ItemWID(
    val id: Int,
    val item: Item
)

class InventoryManager<T>(
    private val items: List<T>,
    private var onItemClick: (position: Int, item: T) -> Unit,
    private val bind: (holder: ItemViewHolder, item: T) -> Unit
) : RecyclerView.Adapter<InventoryManager.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageButton: ImageButton = view.findViewById(R.id.imageButton)
        val text: TextView = view.findViewById(R.id.itemText)
    }

    fun changeOnItemClick(pOnItemClick: (position: Int, item: T) -> Unit) {
        onItemClick = pOnItemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        bind(holder, item)

        holder.imageButton.setOnClickListener {
            onItemClick(position, item)
        }
    }

    override fun getItemCount() = items.size
}