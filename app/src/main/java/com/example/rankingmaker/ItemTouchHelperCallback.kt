package com.example.rankingmaker

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchHelperCallback(
    private val adapter: ItemTouchHelperAdapter
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int = makeMovementFlags(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        0
    )

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    // Pozwala na przeciąganie po dłuższym przytrzymaniu dowolnego elementu
    override fun isLongPressDragEnabled() = true

    override fun isItemViewSwipeEnabled() = false
}
