package com.example.rankingmaker

import java.util.UUID

data class RankingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Int,
    var rank: Int = 0
)
