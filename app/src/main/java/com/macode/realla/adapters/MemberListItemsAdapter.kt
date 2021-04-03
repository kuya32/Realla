package com.macode.realla.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.macode.realla.R
import com.macode.realla.databinding.SingleMemberItemBinding
import com.macode.realla.models.Board
import com.macode.realla.models.User

open class MemberListItemsAdapter(private val context: Context, private val list: ArrayList<User>):
    RecyclerView.Adapter<MemberListItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    inner class ViewHolder(val binding: SingleMemberItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleMemberItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.person)
                .into(binding.memberProfileImage)

            "${model.firstName} ${model.lastName}".also { binding.memberName.text = it }
            binding.memberemail.text = model.email
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

}