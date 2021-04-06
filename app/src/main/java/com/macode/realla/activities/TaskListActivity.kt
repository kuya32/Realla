package com.macode.realla.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.macode.realla.R
import com.macode.realla.adapters.TaskListItemsAdapter
import com.macode.realla.databinding.ActivityTaskListBinding
import com.macode.realla.models.Board
import com.macode.realla.models.Card
import com.macode.realla.models.Task
import com.macode.realla.models.User

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding
    private lateinit var boardDetails: Board
    private lateinit var boardDocumentID: String
    lateinit var assignedMembersDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent.hasExtra("documentID")) {
            boardDocumentID = intent.getStringExtra("documentID").toString()
        }

        showProgressDialog("Loading board info...")
        fireStoreClass.getBoardDetails(this, boardDocumentID)
    }

    // For user experience rather than optimization
//    override fun onResume() {
//        showProgressDialog("Loading board info...")
//        fireStoreClass.getBoardDetails(this, boardDocumentID)
//        super.onResume()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE) {
            showProgressDialog("Loading board info...")
            fireStoreClass.getBoardDetails(this, boardDocumentID)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun setUpToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.taskListToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = boardDetails.name
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#B3FCFF")))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.blue_back)
//        toolbar.setTitleTextColor(Color.parseColor("#430300"))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun boardDetailsUI(board: Board) {
        boardDetails = board
        hideProgressDialog()
        setUpToolbar()

        showProgressDialog("Setting up board...")
        fireStoreClass.getAssignedMembersListDetails(this, boardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog("Loading board...")
        fireStoreClass.getBoardDetails(this, boardDetails.documentID)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(getDate(), taskListName, fireStoreClass.getCurrentUserID())
        boardDetails.taskList.add(0, task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        showProgressDialog("Adding task...")
        fireStoreClass.addUpdateTaskList(this, boardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)

        boardDetails.taskList[position] = task
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        showProgressDialog("Updating task...")
        fireStoreClass.addUpdateTaskList(this, boardDetails)
    }

    fun deleteTaskList(position: Int) {
        boardDetails.taskList.removeAt(position)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        showProgressDialog("Deleting task...")
        fireStoreClass.addUpdateTaskList(this, boardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(fireStoreClass.getCurrentUserID())

        val card = Card(getDate(), cardName, fireStoreClass.getCurrentUserID(), cardAssignedUsersList)

        val cardsList = boardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            boardDetails.taskList[position].dateTaskCreated,
            boardDetails.taskList[position].title,
            boardDetails.taskList[position].createdBy,
            cardsList
        )

        boardDetails.taskList[position] = task

        showProgressDialog("Creating card...")

        fireStoreClass.addUpdateTaskList(this, boardDetails)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra("boardDetails", boardDetails)
        intent.putExtra("taskListItemPosition", taskListPosition)
        intent.putExtra("cardListItemPosition", cardPosition)
        intent.putExtra("boardMembersList", assignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun boardMembersDetailsList(list: ArrayList<User>) {
        assignedMembersDetailList = list
        hideProgressDialog()

        // TODO: Figure out why we need to add an extra task card
        val addTaskList = Task("Add List")
        boardDetails.taskList.add(addTaskList)

        binding.taskListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.taskListRecyclerView.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, boardDetails.taskList)
        binding.taskListRecyclerView.adapter = adapter
    }

    fun updatedCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        boardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog("Moving cards...")
        fireStoreClass.addUpdateTaskList(this, boardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_task_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.actionMembers -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra("boardDetails", boardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}