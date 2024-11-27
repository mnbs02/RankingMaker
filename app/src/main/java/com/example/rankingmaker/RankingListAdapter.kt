package com.example.rankingmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RankingListAdapter(
    private val rankings: MutableList<String>,
    private val onItemClick: (String, Int) -> Unit,
    private val onDeleteClick: (String, Int) -> Unit
) : RecyclerView.Adapter<RankingListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.rankingNameText)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteRankingButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ranking = rankings[position]
        holder.nameText.text = ranking
        
        holder.itemView.setOnClickListener {
            onItemClick(ranking, position)
        }
        
        holder.deleteButton.setOnClickListener {
            onDeleteClick(ranking, position)
        }
    }

    override fun getItemCount() = rankings.size
} 