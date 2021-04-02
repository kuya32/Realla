package com.macode.realla.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.macode.realla.databinding.SingleTaskItemBinding
import com.macode.realla.models.Task

open class TaskListItemAdapter(private val context: Context, private var list: ArrayList<Task>):
    RecyclerView.Adapter<TaskListItemAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: SingleTaskItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleTaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val layoutParams = LinearLayout.LayoutParams((parent.width * 0.80).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins((15.toDP().toPx()), 0, (40.toDP().toPx()), 0)
        binding.root.layoutParams = layoutParams
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        with(holder) {
            if (position == list.size - 1) {
                binding.addTaskListButton.visibility = View.VISIBLE
                binding.linearTaskItem.visibility = View.GONE
            } else {
                binding.addTaskListButton.visibility = View.GONE
                binding.linearTaskItem.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}