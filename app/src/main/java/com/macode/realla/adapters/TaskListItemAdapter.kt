package com.macode.realla.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.macode.realla.activities.TaskListActivity
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

            binding.taskListTitle.text = model.title
            binding.addTaskListButton.setOnClickListener {
                binding.addTaskListButton.visibility = View.GONE
                binding.addTaskListNameCardView.visibility = View.VISIBLE
            }

            binding.closeListNameButton.setOnClickListener {
                binding.addTaskListButton.visibility = View.VISIBLE
                binding.addTaskListNameCardView.visibility = View.GONE
            }

            binding.confirmListName.setOnClickListener {
                val listName = binding.taskListName.text.toString()
                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    showError(binding.taskListName, "Please enter list name!")
                }
            }

            binding.editListNameButton.setOnClickListener {
                binding.editTaskListName.setText(model.title)
                binding.constraintTitleView.visibility = View.GONE
                binding.editTaskListNameCardView.visibility = View.VISIBLE
            }

            binding.closeEditableViewButton.setOnClickListener {
                binding.constraintTitleView.visibility = View.VISIBLE
                binding.editTaskListNameCardView.visibility = View.GONE
            }

            binding.confirmEditableViewButton.setOnClickListener {
                val listName = binding.editTaskListName.text.toString()
                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                } else {
                    showError(binding.taskListName, "Please enter list name!")
                }
            }

            binding.deleteListNameButton.setOnClickListener {
                alertDialogForDeleteTaskList(position, model.title)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun showError(layout: EditText, text: String) {
        layout.error = text
        layout.requestFocus()
    }

    private fun alertDialogForDeleteTaskList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Warning!")
        builder.setMessage("Are you sure you want to delete $title?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") {dialogInterface, which ->
            dialogInterface.dismiss()
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("No") {dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}