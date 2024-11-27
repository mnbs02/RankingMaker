package com.example.rankingmaker

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RankingAdapter(
    private val items: MutableList<RankingItem>,
    private val onItemMoved: (Int, Int) -> Unit,
    private val onEditClick: (RankingItem, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>(), ItemTouchHelperAdapter {

    private var draggedPosition: Int = -1
    private var targetPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isNotEmpty() && payloads[0] == "UPDATE_RANK_ONLY") {
            holder.updateRankOnly()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount() = items.size

    fun updateRanks() {
        items.forEachIndexed { index, item ->
            item.rank = index + 1
        }
        notifyDataSetChanged()
    }

    private fun validateRanks(): Boolean {
        val ranks = items.map { it.rank }.sorted()
        return ranks == (1..items.size).toList()
    }

    override fun onItemDragging(fromPosition: Int, toPosition: Int) {
        draggedPosition = fromPosition
        targetPosition = toPosition
        
        // Tworzymy tymczasową listę rankingów
        val tempRanks = MutableList(items.size) { it + 1 }
        
        if (fromPosition < toPosition) {
            // Przeciągamy w dół
            // Usuwamy rank przeciąganego elementu
            tempRanks.removeAt(fromPosition)
            // Wstawiamy go na nowej pozycji
            tempRanks.add(toPosition, toPosition + 1)
        } else if (fromPosition > toPosition) {
            // Przeciągamy w górę
            // Usuwamy rank przeciąganego elementu
            tempRanks.removeAt(fromPosition)
            // Wstawiamy go na nowej pozycji
            tempRanks.add(toPosition, toPosition + 1)
        }
        
        // Przypisujemy nowe rankingi
        items.forEachIndexed { index, item ->
            item.rank = tempRanks[index]
        }
        
        // Sprawdzamy poprawność rankingów
        if (!validateRanks()) {
            // Jeśli coś jest nie tak, przywracamy prawidłową numerację
            items.forEachIndexed { index, item ->
                item.rank = index + 1
            }
        }
        
        notifyItemRangeChanged(0, itemCount, "UPDATE_RANK_ONLY")
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun finalizeMove(fromPosition: Int, toPosition: Int) {
        // Po zakończeniu przeciągania aktualizujemy wszystkie pozycje
        items.forEachIndexed { index, item ->
            item.rank = index + 1
        }
        
        draggedPosition = -1
        targetPosition = -1
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

        fun updateRankOnly() {
            val currentPosition = adapterPosition
            val item = items[currentPosition]
            rankTextView.text = item.rank.toString()
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
