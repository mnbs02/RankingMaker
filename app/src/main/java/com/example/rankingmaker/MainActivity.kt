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

import android.widget.ImageButton

import com.google.android.material.bottomsheet.BottomSheetBehavior



class MainActivity : AppCompatActivity() {

    private lateinit var rankingAdapter: RankingAdapter

    private lateinit var recyclerView: RecyclerView

    private val rankingItems = mutableListOf<RankingItem>()

    private lateinit var touchHelper: ItemTouchHelper

    private lateinit var rankingStorage: RankingStorage

    private lateinit var rankingTitle: TextView

    private var isButtonsLayoutVisible = true

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var lastState = BottomSheetBehavior.STATE_COLLAPSED



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)



        rankingTitle = findViewById(R.id.rankingTitle)

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



        val rankingOptionsLayout = findViewById<LinearLayout>(R.id.rankingOptionsLayout)

        findViewById<MaterialButton>(R.id.rankingOptionsButton).setOnClickListener {

            if (rankingOptionsLayout.visibility == View.VISIBLE) {

                rankingOptionsLayout.visibility = View.GONE

            } else {

                rankingOptionsLayout.visibility = View.VISIBLE

            }

        }



        findViewById<MaterialButton>(R.id.newRankingButton).setOnClickListener { showNewRankingDialog() }

        findViewById<MaterialButton>(R.id.saveRankingButton).setOnClickListener { showSaveRankingDialog() }

        findViewById<MaterialButton>(R.id.loadRankingButton).setOnClickListener { showLoadRankingDialog() }

        findViewById<MaterialButton>(R.id.addItemButton).setOnClickListener { showAddItemDialog() }

        findViewById<MaterialButton>(R.id.clearRankingButton).setOnClickListener { showClearRankingDialog() }



        loadLastOpenedRanking()



        val buttonsLayout = findViewById<LinearLayout>(R.id.buttonsLayout)

        val permanentHandle = findViewById<View>(R.id.permanentHandle)

        bottomSheetBehavior = BottomSheetBehavior.from(buttonsLayout)

        

        // Obsługa stałego uchwytu

        permanentHandle.setOnClickListener {

            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            }

        }



        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                when (newState) {

                    BottomSheetBehavior.STATE_HIDDEN -> {

                        lastState = BottomSheetBehavior.STATE_HIDDEN

                        permanentHandle.animate()

                            .alpha(1f)

                            .setDuration(200)

                            .start()

                    }

                    else -> {

                        permanentHandle.animate()

                            .alpha(0f)

                            .setDuration(200)

                            .start()

                    }

                }

            }



            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                // Opcjonalnie: możesz dodać animacje podczas przesuwania

                val addItemButton = findViewById<MaterialButton>(R.id.addItemButton)

                val rankingOptionsLayout = findViewById<LinearLayout>(R.id.rankingOptionsLayout)

                

                // Płynne pokazywanie/ukrywanie opcji rankingu

                if (slideOffset > 0) { // Gdy panel jest rozciągany

                    rankingOptionsLayout.visibility = View.VISIBLE

                    rankingOptionsLayout.alpha = slideOffset

                } else { // Gdy panel jest zwijany

                    rankingOptionsLayout.alpha = 0f

                    rankingOptionsLayout.visibility = View.GONE

                }

                

                // Aktualizacja widoczności stałego uchwytu

                if (slideOffset < -0.9f) { // Prawie całkowicie schowany

                    permanentHandle.alpha = (-slideOffset - 0.9f) * 10 // Płynne pojawianie

                } else {

                    permanentHandle.alpha = 0f

                }

            }

        })

    }



    private fun loadLastOpenedRanking() {

        rankingStorage.getLastOpenedRanking()?.let { fileName ->

            rankingStorage.loadRanking(fileName)?.let { items ->

                rankingItems.clear()

                rankingItems.addAll(items)

                rankingAdapter.notifyDataSetChanged()

                rankingTitle.text = fileName.removeSuffix(".json")

            }

        }

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

        val rankingName = rankingTitle.text.toString()

        if (rankingName.isNotEmpty() && rankingName != "RankingMaker") {

            val fullFileName = "$rankingName.json"

            rankingStorage.saveRanking(fullFileName, rankingItems)

            rankingStorage.saveLastOpenedRanking(fullFileName)

            Toast.makeText(this, "Ranking zapisany", Toast.LENGTH_SHORT).show()

        } else {

            // Jeśli nazwa nie jest ustawiona, pokaż dialog z prośbą o nazwę

            val input = EditText(this)

            input.hint = "Nazwa rankingu"



            MaterialAlertDialogBuilder(this)

                .setTitle("Zapisz ranking")

                .setMessage("Najpierw nadaj nazwę rankingowi")

                .setView(input)

                .setPositiveButton("Zapisz") { _, _ ->

                    val fileName = input.text.toString().trim()

                    if (fileName.isNotEmpty()) {

                        val fullFileName = "$fileName.json"

                        rankingStorage.saveRanking(fullFileName, rankingItems)

                        rankingStorage.saveLastOpenedRanking(fullFileName)

                        rankingTitle.text = fileName

                        Toast.makeText(this, "Ranking zapisany", Toast.LENGTH_SHORT).show()

                    }

                }

                .setNegativeButton("Anuluj", null)

                .show()

        }

    }



    private fun showLoadRankingDialog() {

        val rankings = rankingStorage.getRankingFiles()

        if (rankings.isEmpty()) {

            Toast.makeText(this, "Brak zapisanych rankingów", Toast.LENGTH_SHORT).show()

            return

        }



        val dialog = AlertDialog.Builder(this)

            .setTitle("Wybierz ranking")

            .setNegativeButton("Anuluj", null)

            .create()



        val listView = RecyclerView(this).apply {

            layoutManager = LinearLayoutManager(this@MainActivity)

            adapter = RankingListAdapter(

                rankings = rankings.toMutableList(),

                onItemClick = { fileName, position ->

                    val fullFileName = "$fileName.json"

                    rankingStorage.loadRanking(fullFileName)?.let { loadedItems ->

                        rankingItems.clear()

                        rankingItems.addAll(loadedItems)

                        rankingAdapter.notifyDataSetChanged()

                        rankingStorage.saveLastOpenedRanking(fullFileName)

                        rankingTitle.text = fileName

                        dialog.dismiss()

                    }

                },

                onDeleteClick = { fileName, position ->

                    showDeleteRankingConfirmationDialog(fileName, position, dialog)

                }

            )

            setPadding(0, 16, 0, 16)

        }



        dialog.setView(listView)

        dialog.show()

    }



    private fun showDeleteRankingConfirmationDialog(fileName: String, position: Int, parentDialog: AlertDialog) {

        MaterialAlertDialogBuilder(this)

            .setTitle("Usuń ranking")

            .setMessage("Czy na pewno chcesz usunąć ranking \"$fileName\"?")

            .setPositiveButton("Usuń") { _, _ ->

                val fullFileName = "$fileName.json"

                if (rankingStorage.deleteRanking(fullFileName)) {

                    Toast.makeText(this, "Ranking usunięty", Toast.LENGTH_SHORT).show()

                    // Odśwież listę rankingów

                    parentDialog.dismiss()

                    showLoadRankingDialog()

                } else {

                    Toast.makeText(this, "Błąd podczas usuwania rankingu", Toast.LENGTH_SHORT).show()

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



    private fun showNewRankingDialog() {

        val input = EditText(this)

        input.hint = "Nazwa rankingu"



        MaterialAlertDialogBuilder(this)

            .setTitle("Nowy ranking")

            .setView(input)

            .setPositiveButton("Utwórz") { _, _ ->

                val name = input.text.toString().trim()

                if (name.isNotEmpty()) {

                    rankingTitle.text = name

                    rankingItems.clear()

                    rankingAdapter.notifyDataSetChanged()

                }

            }

            .setNegativeButton("Anuluj", null)

            .show()

    }

}


