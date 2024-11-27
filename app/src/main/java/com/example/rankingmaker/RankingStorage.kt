package com.example.rankingmaker

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class RankingStorage(private val context: Context) {
    private val gson = Gson()
    private val sharedPreferences = context.getSharedPreferences("RankingPrefs", Context.MODE_PRIVATE)
    private val LAST_OPENED_KEY = "last_opened_ranking"

    fun saveRanking(fileName: String, items: List<RankingItem>) {
        val json = gson.toJson(items)
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    fun loadRanking(fileName: String): List<RankingItem>? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null

        return try {
            val json = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<RankingItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun getRankingFiles(): List<String> {
        return context.filesDir.listFiles()
            ?.filter { it.isFile && it.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }

    fun saveLastOpenedRanking(fileName: String) {
        sharedPreferences.edit().putString(LAST_OPENED_KEY, fileName).apply()
    }

    fun getLastOpenedRanking(): String? {
        return sharedPreferences.getString(LAST_OPENED_KEY, null)
    }

    fun deleteRanking(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        val deleted = file.delete()
        
        if (deleted && getLastOpenedRanking() == fileName) {
            sharedPreferences.edit().remove(LAST_OPENED_KEY).apply()
        }
        
        return deleted
    }
} 