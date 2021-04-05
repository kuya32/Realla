package com.macode.realla.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.azeesoft.lib.colorpicker.ColorPickerDialog
import com.macode.realla.R
import com.macode.realla.adapters.CardMemberListItemsAdapter
import com.macode.realla.databinding.ActivityCardDetailsBinding
import com.macode.realla.dialogs.MembersListDialog
import com.macode.realla.models.*
import java.lang.StringBuilder

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var boardDetails: Board
    private lateinit var membersDetailsList: ArrayList<User>
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

        binding.selectMembersButton.setOnClickListener {
            membersListDialog()
        }

        setUpSelectedMembersList()
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
        if (intent.hasExtra("boardMembersList")) {
            membersDetailsList = intent.getParcelableArrayListExtra("boardMembersList")!!
        }
    }

    private fun setUpCardDetails() {
        val cardName = boardDetails.taskList[taskListPosition].cards[cardPosition].name
        binding.cardNameEditInput.setText(cardName)
        binding.cardNameEditInput.setSelection(binding.cardNameEditInput.text.toString().length)
        val cardColor = boardDetails.taskList[taskListPosition].cards[cardPosition].labelColor
        if (cardColor != "") {
            binding.selectColorButton.setBackgroundColor(Color.parseColor(cardColor.toString()))
            binding.selectColorButton.text = ""
        }
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

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)

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

    private fun membersListDialog() {
        var cardAssignedMembersList = boardDetails.taskList[taskListPosition].cards[cardPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (i in membersDetailsList.indices) {
                for (j in cardAssignedMembersList) {
                    if (membersDetailsList[i].id == j) {
                        membersDetailsList[i].selected = true
                    }
                }
            }
        } else {
            for (i in membersDetailsList.indices) {
                membersDetailsList[i].selected = false
            }
        }

        val listDialog = object: MembersListDialog(this, membersDetailsList, "Select Member") {
            override fun onItemSelected(user: User, action: String) {
                if (action == "Select") {
                    if (!boardDetails.taskList[taskListPosition].cards[cardPosition].assignedTo.contains(user.id)) {
                        boardDetails.taskList[taskListPosition].cards[cardPosition].assignedTo.add(user.id)
                    }
                } else {
                    boardDetails.taskList[taskListPosition].cards[cardPosition].assignedTo.remove(user.id)

                    for (i in membersDetailsList.indices) {
                        if (membersDetailsList[i].id == user.id) {
                            membersDetailsList[i].selected = false
                        }
                    }
                }

                setUpSelectedMembersList()
            }

        }
        listDialog.window?.setLayout(1000.toDP().toPx(), 600.toDP().toPx())
        listDialog.show()
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

    private fun setUpSelectedMembersList() {
        val cardAssignedMemberList = boardDetails.taskList[taskListPosition].cards[cardPosition].assignedTo
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
        for (i in membersDetailsList.indices) {
            for (j in cardAssignedMemberList) {
                if (membersDetailsList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        membersDetailsList[i].id,
                        membersDetailsList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("", ""))
            binding.selectMembersButton.visibility = View.GONE
            binding.selectedMembersListRecyclerView.visibility = View.VISIBLE

            binding.selectedMembersListRecyclerView.layoutManager = GridLayoutManager(this, 6)
            val adapter = CardMemberListItemsAdapter(this, selectedMembersList, true)
            binding.selectedMembersListRecyclerView.adapter = adapter
            adapter.setOnClickListener(
                object: CardMemberListItemsAdapter.OnClickListener {
                    override fun onClick() {
                        membersListDialog()
                    }

                }
            )
        } else {
            binding.selectMembersButton.visibility = View.VISIBLE
            binding.selectedMembersListRecyclerView.visibility = View.GONE
        }
    }

    private fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}