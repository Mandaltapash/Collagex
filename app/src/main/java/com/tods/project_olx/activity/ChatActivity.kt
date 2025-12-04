package com.tods.project_olx.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tods.project_olx.R
import com.tods.project_olx.adapter.AdapterMessage
import com.tods.project_olx.databinding.ActivityChatBinding
import com.tods.project_olx.model.Ad
import com.tods.project_olx.model.ChatThread
import com.tods.project_olx.model.Message
import com.tods.project_olx.model.User
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesRef: DatabaseReference
    private lateinit var threadRef: DatabaseReference
    private lateinit var userChatsRef: DatabaseReference

    private val messages: MutableList<Message> = mutableListOf()
    private lateinit var adapterMessage: AdapterMessage

    private var threadId: String = ""
    private var ad: Ad? = null
    private lateinit var sellerId: String
    private lateinit var buyerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Chat"

        val currentUser = User().configCurrentUser()
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to chat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        buyerId = currentUser.uid

        val adExtra = intent.getSerializableExtra("selectedAd") as? Ad
        val threadIdExtra = intent.getStringExtra("threadId")

        if (adExtra != null) {
            ad = adExtra
            sellerId = adExtra.sellerId
            if (sellerId.isEmpty()) {
                Toast.makeText(this, "Seller not available", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            // Deterministic thread id: seller_buyer_ad
            threadId = "${sellerId}_${buyerId}_${adExtra.id}"
        } else if (!threadIdExtra.isNullOrEmpty()) {
            threadId = threadIdExtra
            // sellerId and buyerId will be resolved from existing thread when needed
            sellerId = ""
        } else {
            Toast.makeText(this, "Missing chat context", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapterMessage = AdapterMessage(messages)
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerMessages.adapter = adapterMessage

        val db = FirebaseDatabase.getInstance()
        messagesRef = db.getReference("chatMessages").child(threadId)
        threadRef = db.getReference("chatThreads").child(threadId)
        userChatsRef = db.getReference("userChats")

        observeMessages()
        if (ad != null) {
            ensureThreadExists()
        }
        configSendButton()
        configBottomNav()
    }

    private fun observeMessages() {
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (ds in snapshot.children) {
                    val msg = ds.getValue(Message::class.java)
                    if (msg != null) {
                        messages.add(msg)
                    }
                }
                messages.sortBy { it.sentAt }
                adapterMessage.notifyDataSetChanged()
                if (messages.isNotEmpty()) {
                    binding.recyclerMessages.scrollToPosition(messages.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // ignore
            }
        })
    }

    private fun ensureThreadExists() {
        threadRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() && ad != null) {
                    val imageUrl = ad?.adImages?.firstOrNull() ?: ""
                    val chatThread = ChatThread(
                        id = threadId,
                        adId = ad!!.id,
                        adTitle = ad!!.title,
                        adImageUrl = imageUrl,
                        buyerId = buyerId,
                        sellerId = sellerId,
                        lastMessage = "",
                        lastSenderId = "",
                        lastUpdatedAt = System.currentTimeMillis()
                    )
                    threadRef.setValue(chatThread)

                    // Link thread to both users
                    userChatsRef.child(buyerId).child(threadId).setValue(true)
                    userChatsRef.child(sellerId).child(threadId).setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // ignore
            }
        })
    }

    private fun configSendButton() {
        binding.buttonSend.setOnClickListener(View.OnClickListener {
            val text = binding.editMessage.text.toString().trim()
            if (text.isEmpty()) return@OnClickListener

            val messageId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val message = Message(
                id = messageId,
                threadId = threadId,
                senderId = buyerId,
                text = text,
                sentAt = now
            )

            messagesRef.child(messageId).setValue(message)

            // Update thread summary
            val updates = hashMapOf<String, Any>(
                "lastMessage" to text,
                "lastSenderId" to buyerId,
                "lastUpdatedAt" to now
            )
            threadRef.updateChildren(updates)

            binding.editMessage.setText("")
        })
    }

    private fun configBottomNav() {
        binding.bottomNavigation.selectedItemId = R.id.nav_chats
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_chats -> {
                    // Already on chat page
                    true
                }
                R.id.nav_sell -> {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(applicationContext, RegisterAddActivity::class.java))
                    }
                    true
                }
                R.id.nav_my_ads -> {
                    startActivity(Intent(applicationContext, MyAdsActivity::class.java))
                    true
                }
                R.id.nav_account -> {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }
}
