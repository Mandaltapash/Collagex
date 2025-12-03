package com.tods.project_olx.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tods.project_olx.databinding.ItemChatThreadBinding
import com.tods.project_olx.model.ChatThread

class AdapterChatThread(
    private val threads: List<ChatThread>
) : RecyclerView.Adapter<AdapterChatThread.ChatThreadViewHolder>() {

    inner class ChatThreadViewHolder(val binding: ItemChatThreadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatThreadViewHolder {
        val binding = ItemChatThreadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatThreadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatThreadViewHolder, position: Int) {
        val thread = threads[position]
        holder.binding.textChatTitle.text = thread.adTitle
        holder.binding.textChatLastMessage.text = thread.lastMessage
        if (thread.adImageUrl.isNotEmpty()) {
            Picasso.get().load(thread.adImageUrl).into(holder.binding.imageChatAd)
        }
    }

    override fun getItemCount(): Int = threads.size
}
