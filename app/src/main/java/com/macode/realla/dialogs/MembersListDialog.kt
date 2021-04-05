package com.macode.realla.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.macode.realla.R
import com.macode.realla.adapters.MemberListItemsAdapter
import com.macode.realla.databinding.DialogListBinding
import com.macode.realla.models.User

abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
): Dialog(context) {
    private var adapter: MemberListItemsAdapter? = null
    private lateinit var binding: DialogListBinding

    // TODO: Might need to change the binding view to layout inflater
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())
        binding = DialogListBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: Any) {
        binding.dialogTitle.text = title

        if (list.size > 0) {

            binding.dialogListRecyclerView.layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemsAdapter(context, list)
            binding.dialogListRecyclerView.adapter = adapter

            adapter!!.setOnClickListener(object: MemberListItemsAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)
}