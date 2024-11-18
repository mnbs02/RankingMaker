package com.example.rankingmaker

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import yuku.ambilwarna.AmbilWarnaDialog

class MainActivity : AppCompatActivity() {
    private lateinit var rankingAdapter: RankingAdapter
    private lateinit var recyclerView: RecyclerView
    private val rankingItems = mutableListOf<RankingItem>()
    private var selectedColor = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rankingAdapter = RankingAdapter(rankingItems) { fromPos, toPos ->
            // Opcjonalna dodatkowa logika po przesunięciu
        }

        recyclerView = findViewById(R.id.rankingRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = rankingAdapter

        val touchHelper = ItemTouchHelper(ItemTouchHelperCallback(rankingAdapter))
        touchHelper.attachToRecyclerView(recyclerView)

        val addButton: Button = findViewById(R.id.addItemButton)
        addButton.setOnClickListener {
            showAddItemDialog()
        }
    }

    private fun showAddItemDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val nameInput: EditText = dialogView.findViewById(R.id.itemNameInput)
        val colorButton: Button = dialogView.findViewById(R.id.colorPickerButton)

        colorButton.setBackgroundColor(selectedColor)
        colorButton.setOnClickListener {
            val colorPicker = AmbilWarnaDialog(this, selectedColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {}
                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    selectedColor = color
                    colorButton.setBackgroundColor(selectedColor)
                }
            })
            colorPicker.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Dodaj nowy element")
            .setView(dialogView)
            .setPositiveButton("Dodaj") { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    val newItem = RankingItem(name = name, color = selectedColor, rank = rankingItems.size + 1)
                    rankingItems.add(newItem)
                    rankingAdapter.notifyItemInserted(rankingItems.size - 1)
                } else {
                    Toast.makeText(this, "Nazwa nie może być pusta", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }
}