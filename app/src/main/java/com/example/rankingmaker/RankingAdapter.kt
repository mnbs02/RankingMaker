package com.example.rankingmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
}

class RankingAdapter(
    private val items: MutableList<RankingItem>,
    private val onItemMoved: (Int, Int) -> Unit
) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(items, fromPosition, toPosition)
        updateRanks()
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun updateRanks() {
        items.forEachIndexed { index, item ->
            item.rank = index + 1
        }
    }

    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.itemName)
        private val rankTextView: TextView = itemView.findViewById(R.id.itemRank)
        private val colorView: View = itemView.findViewById(R.id.itemColor)

        fun bind(item: RankingItem) {
            nameTextView.text = item.name
            rankTextView.text = "Pozycja: ${item.rank}"
            colorView.setBackgroundColor(item.color)
        }
    }
}