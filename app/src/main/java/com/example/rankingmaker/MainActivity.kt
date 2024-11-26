package com.example.rankingmaker

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import yuku.ambilwarna.AmbilWarnaDialog
import kotlin.random.Random
import android.content.res.ColorStateList

class MainActivity : AppCompatActivity() {
    private lateinit var rankingAdapter: RankingAdapter
    private lateinit var recyclerView: RecyclerView
    private val rankingItems = mutableListOf<RankingItem>()
    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var rankingStorage: RankingStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rankingStorage = RankingStorage(this)
        rankingAdapter = RankingAdapter(
            rankingItems,
            { _, _ -> },
            { item, position -> showEditItemDialog(item, position) },
            { position -> showDeleteConfirmationDialog(position) }
        )
        recyclerView = findViewById(R.id.rankingRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = rankingAdapter

        touchHelper = ItemTouchHelper(ItemTouchHelperCallback(rankingAdapter))
        touchHelper.attachToRecyclerView(recyclerView)

        findViewById<MaterialButton>(R.id.saveRankingButton).setOnClickListener { showSaveRankingDialog() }
        findViewById<MaterialButton>(R.id.loadRankingButton).setOnClickListener { showLoadRankingDialog() }
        findViewById<MaterialButton>(R.id.addItemButton).setOnClickListener { showAddItemDialog() }
        findViewById<MaterialButton>(R.id.clearRankingButton).setOnClickListener { showClearRankingDialog() }
    }

    private fun generateRandomColor(): Int {
        return Color.rgb(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )
    }

    private fun updateDialogColors(dialog: AlertDialog, color: Int) {
        // Ustawienie koloru tła dialogu
        dialog.window?.setBackgroundDrawable(ColorDrawable(color))
        
        // Obliczanie kontrastowego koloru tekstu
        val brightness = (Color.red(color) * 299 + 
                        Color.green(color) * 587 + 
                        Color.blue(color) * 114) / 1000
        val textColor = if (brightness >= 128) Color.BLACK else Color.WHITE
        
        // Ustawienie kolorów dla przycisku
        val colorButton = dialog.findViewById<MaterialButton>(R.id.colorPickerButton)
        colorButton?.apply {
            setTextColor(textColor)
            setStrokeColor(ColorStateList.valueOf(textColor))
        }

        // Ustawienie kolorów dla przycisków dialogu
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(textColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(textColor)
        
        // Ustawienie koloru tytułu
        val titleId = dialog.context.resources.getIdentifier("alertTitle", "id", "android")
        dialog.findViewById<TextView>(titleId)?.setTextColor(textColor)
    }

    private fun showSaveRankingDialog() {
        val input = EditText(this)
        input.hint = "Nazwa rankingu"

        MaterialAlertDialogBuilder(this)
            .setTitle("Zapisz ranking")
            .setView(input)
            .setPositiveButton("Zapisz") { _, _ ->
                val fileName = input.text.toString().trim()
                if (fileName.isNotEmpty()) {
                    rankingStorage.saveRanking("$fileName.json", rankingItems)
                    Toast.makeText(this, "Ranking zapisany", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showLoadRankingDialog() {
        val rankings = rankingStorage.getRankingFiles()
        if (rankings.isEmpty()) {
            Toast.makeText(this, "Brak zapisanych rankingów", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Wybierz ranking")
            .setItems(rankings.toTypedArray()) { _, which ->
                val fileName = "${rankings[which]}.json"
                rankingStorage.loadRanking(fileName)?.let { items ->
                    rankingItems.clear()
                    rankingItems.addAll(items)
                    rankingAdapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showAddItemDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val nameInput: EditText = dialogView.findViewById(R.id.itemNameInput)
        val colorButton: MaterialButton = dialogView.findViewById(R.id.colorPickerButton)

        val randomColor = generateRandomColor()
        var selectedColor = randomColor

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Dodaj nowy element")
            .setView(dialogView)
            .setPositiveButton("Dodaj", null)
            .setNegativeButton("Anuluj", null)
            .create()

        alertDialog.setOnShowListener {
            updateDialogColors(alertDialog, selectedColor)
            
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    val newItem = RankingItem(
                        name = name,
                        color = selectedColor,
                        rank = rankingItems.size + 1
                    )
                    rankingItems.add(newItem)
                    rankingAdapter.notifyItemInserted(rankingItems.size - 1)
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(this, "Nazwa nie może być pusta", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val colorPicker = AmbilWarnaDialog(this, selectedColor,
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {}
                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    selectedColor = color
                    updateDialogColors(alertDialog, color)
                }
            })

        colorButton.setOnClickListener {
            colorPicker.show()
        }

        alertDialog.show()
    }

    private fun showEditItemDialog(item: RankingItem, position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val nameInput: EditText = dialogView.findViewById(R.id.itemNameInput)
        val colorButton: MaterialButton = dialogView.findViewById(R.id.colorPickerButton)

        nameInput.setText(item.name)
        var selectedColor = item.color

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Edytuj element")
            .setView(dialogView)
            .setPositiveButton("Zapisz", null)
            .setNegativeButton("Anuluj", null)
            .create()

        alertDialog.setOnShowListener {
            updateDialogColors(alertDialog, selectedColor)
            
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    rankingItems[position] = RankingItem(
                        name = name,
                        color = selectedColor,
                        rank = item.rank
                    )
                    rankingAdapter.notifyItemChanged(position)
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(this, "Nazwa nie może być pusta", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val colorPicker = AmbilWarnaDialog(this, selectedColor,
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {}
                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    selectedColor = color
                    updateDialogColors(alertDialog, color)
                }
            })

        colorButton.setOnClickListener {
            colorPicker.show()
        }

        alertDialog.show()
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Usuń element")
            .setMessage("Czy na pewno chcesz usunąć ten element?")
            .setPositiveButton("Usuń") { _, _ ->
                rankingItems.removeAt(position)
                rankingAdapter.notifyItemRemoved(position)
                rankingAdapter.updateRanks()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showClearRankingDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Wyczyść ranking")
            .setMessage("Czy na pewno chcesz wyczyścić cały ranking?")
            .setPositiveButton("Wyczyść") { _, _ ->
                rankingItems.clear()
                rankingAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }
}
