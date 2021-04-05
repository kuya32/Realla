package com.macode.realla.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.macode.realla.R
import com.macode.realla.databinding.SingleSelectedCardMemberItemBinding
import com.macode.realla.models.SelectedMembers
import com.macode.realla.models.User

open class CardMemberListItemsAdapter(private val context: Context, private val list: ArrayList<SelectedMembers>, private val assignMembers: Boolean):
    RecyclerView.Adapter<CardMemberListItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    inner class ViewHolder(val binding: SingleSelectedCardMemberItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleSelectedCardMemberItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            if (position == list.size - 1 && assignMembers) {
                binding.addMemberButton.visibility = View.VISIBLE
                binding.selectedMemberImage.visibility = View.GONE
            } else {
                binding.addMemberButton.visibility = View.GONE
                binding.selectedMemberImage.visibility = View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.person)
                    .into(binding.selectedMemberImage)
            }

            itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
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
        fun onClick()
    }
}