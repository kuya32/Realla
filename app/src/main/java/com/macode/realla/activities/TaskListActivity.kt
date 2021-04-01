package com.macode.realla.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.macode.realla.R
import com.macode.realla.models.Board

class TaskListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        var boardDocumentID = ""
        if (intent.hasExtra("documentID")) {
            boardDocumentID = intent.getStringExtra("documentID").toString()
        }

        showProgressDialog("Loading board info...")
        fireStoreClass.getBoardDetails(this, boardDocumentID)
    }

    fun setUpToolbar(title: String) {
        val toolbar = findViewById<Toolbar>(R.id.taskListToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.blue_back)
//        toolbar.setTitleTextColor(Color.parseColor("#430300"))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun boardDetails(board: Board) {
        hideProgressDialog()
        setUpToolbar(board.name)
    }
}