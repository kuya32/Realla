package com.macode.realla.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.macode.realla.R
import com.macode.realla.databinding.ActivityCardDetailsBinding
import com.macode.realla.models.Board

class CardDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var boardDetails: Board
    private var taskListPosition = -1
    private var cardPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        getIntentData()
        setUpToolbar()
        setUpCardDetails()
    }

    private fun setUpToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.cardDetailsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        if (intent.hasExtra("cardItemPosition")) {
            taskListPosition = intent.getIntExtra("cardItemPosition", -1)
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

            }
        }
        return super.onOptionsItemSelected(item)
    }
}