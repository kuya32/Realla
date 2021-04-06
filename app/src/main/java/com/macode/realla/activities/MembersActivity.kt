package com.macode.realla.activities

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.macode.realla.R
import com.macode.realla.adapters.MemberListItemsAdapter
import com.macode.realla.databinding.ActivityMembersBinding
import com.macode.realla.databinding.DialogSearchMemberBinding
import com.macode.realla.models.Board
import com.macode.realla.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext


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
        sendNotificationToUser(boardDetails.name, user.fcmToken)
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

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun sendNotificationToUser(boardName: String, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("https://fcm.googleapis.com/fcm/send")
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty("authorization", "key=AAAABDxQeQw:APA91bEfxWQ49HaEtwjgXORukp47M3hzL9wx6sYGQ5VeXoJeGWezeHPOamCHPXlRYIfHhHXWKrmaV2T8dAVKvASSBWdjXGk_ot3nn83lXiIahWtAWrx_SPC9rrxcags9xOxCqTmuo-Hf")

                connection.useCaches = false
                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put("title", "Assigned to the board $boardName")
                dataObject.put("message", "You have been assigned to the Board by ${assignedMembersList[0].username}")

                jsonRequest.put("data", dataObject)
                jsonRequest.put("to", token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also {line = it} != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    Log.i(NOTI, sb.toString())
                } else {
                    Log.i(NOTI, connection.responseMessage)
                }
            } catch (e: SocketTimeoutException) {
                Log.i(NOTI, "Connection Timeout")
            } catch (e: Exception) {
                Log.i(NOTI, "Error: ${e.message}")
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}