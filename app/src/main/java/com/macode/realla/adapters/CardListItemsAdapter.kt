package com.macode.realla.adapters

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macode.realla.R
import com.macode.realla.activities.TaskListActivity
import com.macode.realla.databinding.SingleCardItemBinding
import com.macode.realla.models.Board
import com.macode.realla.models.Card
import com.macode.realla.models.SelectedMembers

open class CardListItemsAdapter(private val context: Context, private var list: ArrayList<Card>):
    RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private val selectedMembersListNames: ArrayList<String> = ArrayList()

    inner class ViewHolder(val binding: SingleCardItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.cardName.text = model.name
            if (model.labelColor != "") {
                binding.cardListDivider.visibility = View.VISIBLE
                binding.cardListDivider.setBackgroundColor(Color.parseColor(model.labelColor.toString()))
            }
            if ((context as TaskListActivity).assignedMembersDetailList.size > 0) {
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()


                for (i in context.assignedMembersDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.assignedMembersDetailList[i].id == j) {
                            val firstName = context.assignedMembersDetailList[i].firstName
                            val lastName = context.assignedMembersDetailList[i].lastName
                            val fullName = "$firstName $lastName"
                            val selectedMembers = SelectedMembers(
                                context.assignedMembersDetailList[i].id,
                                context.assignedMembersDetailList[i].image
                            )
                            selectedMembersList.add(selectedMembers)
                            selectedMembersListNames.add(fullName)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        binding.cardSelectedMembersListRecyclerView.visibility = View.GONE
                    } else {
                        binding.cardMembersNames.visibility = View.VISIBLE
                        val listOfNames = selectedMembersListStringBuilder(selectedMembersListNames)
                        val membersNames = context.resources.getString(R.string.memberNames)
                        "$membersNames $listOfNames".also { binding.cardMembersNames.text = it }

                        binding.cardSelectedMembersListRecyclerView.visibility = View.VISIBLE
                        binding.cardSelectedMembersListRecyclerView.layoutManager = GridLayoutManager(context, 4)
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        binding.cardSelectedMembersListRecyclerView.adapter = adapter
                        adapter.setOnClickListener(object: CardMemberListItemsAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }

                        })
                    }
                } else {
                    binding.cardSelectedMembersListRecyclerView.visibility = View.GONE
                }
            }
            itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    private fun selectedMembersListStringBuilder(list: ArrayList<String>): String {
        var listOfNames: String = ""
        for (i in list) {
            listOfNames = "$listOfNames$i, "
        }
        listOfNames = listOfNames.substring(0, listOfNames.length - 2)
        return listOfNames
    }
}