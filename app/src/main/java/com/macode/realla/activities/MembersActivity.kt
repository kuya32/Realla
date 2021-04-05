package com.macode.realla.activities

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.macode.realla.R
import com.macode.realla.adapters.MemberListItemsAdapter
import com.macode.realla.databinding.ActivityMembersBinding
import com.macode.realla.databinding.DialogSearchMemberBinding
import com.macode.realla.models.Board
import com.macode.realla.models.User


class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var dialogBinding: DialogSearchMemberBinding
    private lateinit var boardDetails: Board
    private lateinit var assignedMembersList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        dialogBinding = DialogSearchMemberBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent.hasExtra("boardDetails")) {
            boardDetails = intent.getParcelableExtra("boardDetails")!!
            showProgressDialog("Loading assigned board members...")
            fireStoreClass.getAssignedMembersListDetails(this, boardDetails.assignedTo)
        }

        setUpToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.addMember -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.membersToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Board Members"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#B3FCFF")))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.blue_back)
//        toolbar.setTitleTextColor(Color.parseColor("#430300"))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUpMemberList(list: ArrayList<User>) {
        assignedMembersList = list

        hideProgressDialog()

        binding.memberListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.memberListRecyclerView.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        binding.memberListRecyclerView.adapter = adapter
    }

    fun memberDetails(user: User) {
        boardDetails.assignedTo.add(user.id)
        fireStoreClass.assignMemberToBoard(this, boardDetails, user)
    }

    fun memberAssignedSuccess(user: User) {
        hideProgressDialog()
        assignedMembersList.add(user)
        anyChangesMade = true
        setUpMemberList(assignedMembersList)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        // TODO: Clicking add member item twice crashes app
        dialog.setContentView(dialogBinding.root)
        dialogBinding.dialogAddButton.setOnClickListener {
            val email = dialogBinding.dialogEmailEdit.text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog("Retrieving member details...")
                fireStoreClass.getMemberDetails(this, email)
            } else {
                showError(dialogBinding.dialogEmail, "Please enter member\'s email address!")
            }
        }
        dialogBinding.dialogCancelButton.setOnClickListener {
            dialog.dismiss()
        }
        // TODO: Make the dialog window layout more dynamic
        dialog.window?.setLayout(1000.toDP().toPx(), 600.toDP().toPx())
        dialog.show()
    }

    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    private fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}