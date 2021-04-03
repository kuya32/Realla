package com.macode.realla.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.macode.realla.databinding.SingleCardItemBinding
import com.macode.realla.models.Board
import com.macode.realla.models.Card

open class CardListItemsAdapter(private val context: Context, private var list: ArrayList<Card>):
    RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    inner class ViewHolder(val binding: SingleCardItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.cardName.text = model.name
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
}