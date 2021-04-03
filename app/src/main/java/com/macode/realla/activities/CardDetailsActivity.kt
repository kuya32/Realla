package com.macode.realla.activities

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.azeesoft.lib.colorpicker.ColorPickerDialog
import com.macode.realla.R
import com.macode.realla.databinding.ActivityCardDetailsBinding
import com.macode.realla.models.Board
import com.macode.realla.models.Card
import com.macode.realla.models.Task
import java.lang.StringBuilder

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var boardDetails: Board
    private var taskListPosition = -1
    private var cardPosition = -1
    private var selectedHexColor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        getIntentData()
        setUpToolbar()
        setUpCardDetails()

        binding.updateCardDetailsButton.setOnClickListener {
            if (binding.cardNameEditInput.text.isNullOrEmpty()) {
                showError(binding.cardNameInput, "Please enter card name!")
            } else {
                updateCardDetails()
            }
        }

        binding.selectColorButton.setOnClickListener {
            colorPalettePickerDialog()
        }
    }

    private fun setUpToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.cardDetailsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        println(taskListPosition)
        println(cardPosition)
        supportActionBar?.title = boardDetails.taskList[taskListPosition].cards[cardPosition].name
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#B3FCFF")))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.blue_back)
//        toolbar.setTitleTextColor(Color.parseColor("#430300"))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getIntentData() {
        if (intent.hasExtra("boardDetails")) {
            boardDetails = intent.getParcelableExtra("boardDetails")!!
        }
        if (intent.hasExtra("taskListItemPosition")) {
            taskListPosition = intent.getIntExtra("taskListItemPosition", -1)
        }
        if (intent.hasExtra("cardListItemPosition")) {
            cardPosition = intent.getIntExtra("cardListItemPosition", -1)
        }
    }

    private fun setUpCardDetails() {
        val cardName = boardDetails.taskList[taskListPosition].cards[cardPosition].name
        binding.cardNameEditInput.setText(cardName)
        binding.cardNameEditInput.setSelection(binding.cardNameEditInput.text.toString().length)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_card_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteCard -> {
                alertDialogForDeleteCard(boardDetails.taskList[taskListPosition].cards[cardPosition].name)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = Card(
            boardDetails.taskList[taskListPosition].cards[cardPosition].dateCardCreated,
            binding.cardNameEditInput.text.toString(),
            boardDetails.taskList[taskListPosition].cards[cardPosition].createdBy,
            boardDetails.taskList[taskListPosition].cards[cardPosition].assignedTo,
            selectedHexColor
        )

        boardDetails.taskList[taskListPosition].cards[cardPosition] = card
        showProgressDialog("Updating card item...")
        fireStoreClass.addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun deleteCard() {
        val cardList: ArrayList<Card> = boardDetails.taskList[taskListPosition].cards
        cardList.removeAt(cardPosition)

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[taskListPosition].cards = cardList

        showProgressDialog("Deleting card...")
        fireStoreClass.addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    //    TODO: Make the alert dialog look cleaner
    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Warning!")
        builder.setMessage("Are you sure you want to delete $cardName?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    // TODO: Customize the color palette picker dialog
    private fun colorPalettePickerDialog() {
        val colorPickerDialog = ColorPickerDialog.createColorPickerDialog(this, R.style.CustomColorPicker)
        colorPickerDialog.setOnColorPickedListener { _, hexVal ->
            selectedHexColor = hexVal.removeRange(1, 3)
            binding.selectColorButton.setBackgroundColor(Color.parseColor(hexVal))
            binding.selectColorButton.text = ""
        }
        colorPickerDialog.show()
    }
}