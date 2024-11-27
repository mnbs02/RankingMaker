package com.example.rankingmaker

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemDragging(fromPosition: Int, toPosition: Int)
} 