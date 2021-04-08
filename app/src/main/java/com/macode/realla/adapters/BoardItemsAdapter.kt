package com.macode.realla.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.macode.realla.R
import com.macode.realla.activities.CreateBoardActivity
import com.macode.realla.activities.MainActivity
import com.macode.realla.databinding.SingleBoardItemBinding
import com.macode.realla.firebase.FireStoreClass
import com.macode.realla.models.Board

open class BoardItemsAdapter (private val context: Context, private var list: ArrayList<Board>):
    RecyclerView.Adapter<BoardItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private val fireStoreClass: FireStoreClass = FireStoreClass()

    inner class ViewHolder(val binding: SingleBoardItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleBoardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        with(holder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.thing)
                .into(binding.singleBoardItemImage)
            binding.singleBoardItemName.text = model.name
            "Created by: ${model.createdBy}".also { binding.singleBoardItemCreatedBy.text = it }

            itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
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

    fun notifyEditItem(mainActivity: MainActivity, position: Int, requestCode: Int) {
        val intent = Intent(context, CreateBoardActivity::class.java)
        intent.putExtra("boardDetails", list[position])
        mainActivity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeAt(activity: MainActivity, position: Int) {
        if (list.size > 1) {
            activity.showProgressDialog("Deleting board...")
            fireStoreClass.deleteBoard(activity, list[position], "boards")
        } else {
            activity.showProgressDialog("Deleting board...")
            fireStoreClass.deleteBoard(activity, list[position], "noBoards")
        }
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }
}