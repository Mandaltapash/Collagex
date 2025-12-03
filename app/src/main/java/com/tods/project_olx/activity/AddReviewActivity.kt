package com.tods.project_olx.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.tods.project_olx.databinding.ActivityAddReviewBinding
import com.tods.project_olx.model.Review
import com.tods.project_olx.model.User

class AddReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReviewBinding
    private lateinit var reviewsByAdRef: DatabaseReference
    private lateinit var reviewsByUserRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var adsAllRef: DatabaseReference

    private var adId: String = ""
    private var sellerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Rate seller"

        val currentUser = User().configCurrentUser()
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adId = intent.getStringExtra("adId") ?: ""
        sellerId = intent.getStringExtra("sellerId") ?: ""

        if (adId.isEmpty() || sellerId.isEmpty()) {
            Toast.makeText(this, "Invalid review context", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = FirebaseDatabase.getInstance()
        reviewsByAdRef = db.getReference("reviewsByAd").child(adId)
        reviewsByUserRef = db.getReference("reviewsByUser").child(sellerId)
        usersRef = db.getReference("users").child(sellerId)
        adsAllRef = db.getReference("ads_all").child(adId)

        binding.buttonSubmitReview.setOnClickListener(View.OnClickListener {
            val ratingValue = binding.ratingBar.rating.toInt()
            val comment = binding.editComment.text.toString().trim()

            if (ratingValue <= 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            saveReview(currentUser.uid, ratingValue, comment)
        })
    }

    private fun saveReview(reviewerId: String, rating: Int, comment: String) {
        val reviewId = reviewsByAdRef.push().key
        if (reviewId == null) {
            Toast.makeText(this, "Error creating review", Toast.LENGTH_SHORT).show()
            return
        }

        val now = System.currentTimeMillis()
        val review = Review(
            id = reviewId,
            adId = adId,
            reviewerId = reviewerId,
            reviewedUserId = sellerId,
            rating = rating,
            comment = comment,
            createdAt = now
        )

        val updates = HashMap<String, Any>()
        updates["/reviewsByAd/$adId/$reviewId"] = review
        updates["/reviewsByUser/$sellerId/$reviewId"] = review

        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.updateChildren(updates).addOnCompleteListener {
            if (it.isSuccessful) {
                updateAggregates(rating)
            } else {
                Toast.makeText(this, "Failed to save review", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAggregates(rating: Int) {
        // Update seller rating
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentAverage = snapshot.child("ratingAverage").getValue(Double::class.java) ?: 0.0
                val currentCount = snapshot.child("ratingCount").getValue(Int::class.java) ?: 0
                val newCount = currentCount + 1
                val newAverage = ((currentAverage * currentCount) + rating) / newCount

                val updates = hashMapOf<String, Any>(
                    "ratingAverage" to newAverage,
                    "ratingCount" to newCount
                )
                usersRef.updateChildren(updates)
            }

            override fun onCancelled(error: DatabaseError) { }
        })

        // Update ad rating stored in ads_all (if present)
        adsAllRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val currentAverage = snapshot.child("ratingAverage").getValue(Double::class.java) ?: 0.0
                val currentCount = snapshot.child("ratingCount").getValue(Int::class.java) ?: 0
                val newCount = currentCount + 1
                val newAverage = ((currentAverage * currentCount) + rating) / newCount

                val updates = hashMapOf<String, Any>(
                    "ratingAverage" to newAverage,
                    "ratingCount" to newCount
                )
                adsAllRef.updateChildren(updates)
            }

            override fun onCancelled(error: DatabaseError) { }
        })

        Toast.makeText(this, "Review submitted", Toast.LENGTH_SHORT).show()
        finish()
    }
}
