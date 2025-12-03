package com.tods.project_olx.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tods.project_olx.databinding.ItemMessageMeBinding
import com.tods.project_olx.databinding.ItemMessageOtherBinding
import com.tods.project_olx.model.Message
import com.tods.project_olx.model.User

class AdapterMessage(
    private val messages: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUserId: String? = User().configCurrentUser()?.uid

    private val TYPE_ME = 1
    private val TYPE_OTHER = 2

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) TYPE_ME else TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ME) {
            val binding = ItemMessageMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MeViewHolder(binding)
        } else {
            val binding = ItemMessageOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            OtherViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is MeViewHolder -> holder.binding.textMessageMe.text = message.text
            is OtherViewHolder -> holder.binding.textMessageOther.text = message.text
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class MeViewHolder(val binding: ItemMessageMeBinding) : RecyclerView.ViewHolder(binding.root)
    inner class OtherViewHolder(val binding: ItemMessageOtherBinding) : RecyclerView.ViewHolder(binding.root)
}
