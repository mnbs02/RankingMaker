package com.example.rankingmaker

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
}

class RankingAdapter(
    private val items: MutableList<RankingItem>,
    private val onItemMoved: (Int, Int) -> Unit,
    private val onEditClick: (RankingItem, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
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

    fun updateRanks() {
        items.forEachIndexed { index, item ->
            item.rank = index + 1
        }
        notifyDataSetChanged()
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        // Tylko wizualne przesunięcie podczas przeciągania
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun finalizeMove(fromPosition: Int, toPosition: Int) {
        // Aktualizujemy rankingi po zakończeniu przeciągania
        items.forEachIndexed { index, rankingItem ->
            rankingItem.rank = index + 1
        }
        notifyDataSetChanged()
    }

    private fun MutableList<RankingItem>.swap(index1: Int, index2: Int) {
        val tmp = this[index1]
        this[index1] = this[index2]
        this[index2] = tmp
    }

    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.itemName)
        private val rankTextView: TextView = itemView.findViewById(R.id.itemRank)
        private val colorView: View = itemView.findViewById(R.id.itemColor)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        init {
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(items[position], position)
                }
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(position)
                }
            }
        }

        fun bind(item: RankingItem) {
            itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .start()

            rankTextView.text = item.rank.toString()
            nameTextView.text = item.name
            colorView.setBackgroundColor(item.color)
        }
    }
}
