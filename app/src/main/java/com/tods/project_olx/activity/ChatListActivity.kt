package com.tods.project_olx.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tods.project_olx.adapter.AdapterChatThread
import com.tods.project_olx.databinding.ActivityChatListBinding
import com.tods.project_olx.helper.RecyclerItemClickListener
import com.tods.project_olx.model.ChatThread
import com.tods.project_olx.model.User

class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding
    private lateinit var userChatsRef: DatabaseReference
    private lateinit var threadsRef: DatabaseReference

    private val threads: MutableList<ChatThread> = mutableListOf()
    private lateinit var adapterChatThread: AdapterChatThread

    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Chats"

        currentUserId = User().configCurrentUser()?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "You must be logged in to view chats", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapterChatThread = AdapterChatThread(threads)
        binding.recyclerThreads.layoutManager = LinearLayoutManager(this)
        binding.recyclerThreads.setHasFixedSize(true)
        binding.recyclerThreads.adapter = adapterChatThread

        binding.recyclerThreads.addOnItemTouchListener(
            RecyclerItemClickListener(this, binding.recyclerThreads,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val thread = threads[position]
                        val intent = Intent(applicationContext, ChatActivity::class.java)
                        intent.putExtra("selectedAd", null as java.io.Serializable?)
                        intent.putExtra("threadId", thread.id)
                        // For now, ChatActivity is primarily started from AdDetails with full Ad; 
                        // list view navigation can be refined later.
                        startActivity(intent)
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        // No long-click actions yet
                    }
                })
        )

        val db = FirebaseDatabase.getInstance()
        userChatsRef = db.getReference("userChats").child(currentUserId!!)
        threadsRef = db.getReference("chatThreads")

        loadThreads()
    }

    private fun loadThreads() {
        userChatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val threadIds = snapshot.children.map { it.key!! }
                if (threadIds.isEmpty()) {
                    threads.clear()
                    adapterChatThread.notifyDataSetChanged()
                    return
                }

                threadsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        threads.clear()
                        for (id in threadIds) {
                            val node = snapshot.child(id)
                            val thread = node.getValue(ChatThread::class.java)
                            if (thread != null) {
                                threads.add(thread)
                            }
                        }
                        threads.sortByDescending { it.lastUpdatedAt }
                        adapterChatThread.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // ignore
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // ignore
            }
        })
    }
}
