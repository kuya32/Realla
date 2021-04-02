package com.macode.realla.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macode.realla.R
import com.macode.realla.adapters.TaskListItemAdapter
import com.macode.realla.databinding.ActivityTaskListBinding
import com.macode.realla.models.Board
import com.macode.realla.models.Task

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding
    private lateinit var boardDetails: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var boardDocumentID = ""
        if (intent.hasExtra("documentID")) {
            boardDocumentID = intent.getStringExtra("documentID").toString()
        }

        showProgressDialog("Loading board info...")
        fireStoreClass.getBoardDetails(this, boardDocumentID)
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

    fun boardDetails(board: Board) {
        boardDetails = board
        hideProgressDialog()
        setUpToolbar()

        val addTaskList = Task("Add List")
        board.taskList.add(addTaskList)

        binding.taskListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.taskListRecyclerView.setHasFixedSize(true)

        val adapter = TaskListItemAdapter(this, board.taskList)
        binding.taskListRecyclerView.adapter = adapter
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
}