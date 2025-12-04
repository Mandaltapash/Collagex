package com.tods.project_olx.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tods.project_olx.databinding.ActivityAdDetailsBinding
import com.tods.project_olx.model.Ad
import com.tods.project_olx.model.User

class AdDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdDetailsBinding
    private lateinit var selectedAd: Ad
    private var sellerUser: User? = null
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configToolbar()
        configViewBinding()
        configAdDetails()
        configCarousel()
        configSellerSection()
        configButtonCallClickListener()
    }

    private fun configButtonCallClickListener() {
        binding.buttonCall.visibility = View.GONE // Hide call button since phone is removed

        binding.buttonBuy.setOnClickListener {
            val currentUser = User().configCurrentUser()
            if (currentUser == null) {
                Toast.makeText(this, "Please log in to buy items", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                return@setOnClickListener
            }

            if (selectedAd.sellerId == currentUser.uid) {
                Toast.makeText(this, "You cannot buy your own ad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedAd.buyerId = currentUser.uid
            selectedAd.status = "sold"
            selectedAd.update()
            Toast.makeText(this, "Ad marked as sold!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun configToolbar() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Ad Details"
    }

    private fun configCarousel() {
        val imageAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val imageView = ImageView(parent.context)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                return object : RecyclerView.ViewHolder(imageView) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val imageView = holder.itemView as ImageView
                val urlString: String = selectedAd.adImages[position]
                Picasso.get().load(urlString).into(imageView)
            }

            override fun getItemCount(): Int {
                return selectedAd.adImages.size
            }
        }
        binding.viewPagerImages.adapter = imageAdapter
    }

    private fun configAdDetails() {
        selectedAd = intent.getSerializableExtra("selectedAd") as Ad
        binding.textAdDetailTitle.text = selectedAd.title
        binding.textAdDetailValue.text = selectedAd.value
        binding.textAdDetailDescription.text = selectedAd.description

        val currentUser = User().configCurrentUser()
        if (currentUser != null && (selectedAd.sellerId == currentUser.uid || selectedAd.status == "sold")) {
            binding.buttonBuy.visibility = View.GONE
        } else {
            binding.buttonBuy.visibility = View.VISIBLE
        }
    }

    private fun configSellerSection() {
        val currentUser = User().configCurrentUser()
        val isOwnAd = currentUser != null && currentUser.uid == selectedAd.sellerId
        
        if (selectedAd.sellerId.isEmpty()) {
            binding.sellerContainer.visibility = View.GONE
            return
        }

        binding.sellerContainer.visibility = View.VISIBLE

        // Load seller details from Firebase
        userRef = FirebaseDatabase.getInstance().getReference("users")
            .child(selectedAd.sellerId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    sellerUser = user
                    binding.textSellerName.text = user.name
                    binding.textSellerLocation.text = if (user.collegeName.isNotEmpty()) user.collegeName else "College not set"

                    val ratingText = if (user.ratingCount > 0) {
                        String.format("%.1f (%d reviews)", user.ratingAverage, user.ratingCount)
                    } else {
                        "No reviews yet"
                    }
                    binding.textSellerRating.text = ratingText

                    if (user.photoUrl.isNotEmpty()) {
                        Picasso.get().load(user.photoUrl).into(binding.imageSellerAvatar)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdDetailsActivity, "Failed to load seller", Toast.LENGTH_SHORT).show()
            }
        })

        // Show Edit button for own ads, Profile/Message for others' ads
        if (isOwnAd) {
            // User's own ad - show Edit button only
            binding.buttonEditAd.visibility = View.VISIBLE
            binding.buttonViewProfile.visibility = View.GONE
            binding.buttonMessageSeller.visibility = View.GONE
            binding.buttonRateSeller.visibility = View.GONE
            
            binding.buttonEditAd.setOnClickListener {
                val intent = Intent(this, EditAdActivity::class.java)
                intent.putExtra("adToEdit", selectedAd)
                startActivity(intent)
            }
        } else {
            // Other user's ad - show Profile/Message buttons
            binding.buttonEditAd.visibility = View.GONE
            binding.buttonViewProfile.visibility = View.VISIBLE
            binding.buttonMessageSeller.visibility = View.VISIBLE
            
            binding.buttonViewProfile.setOnClickListener {
                val sellerId = selectedAd.sellerId
                if (sellerId.isNotEmpty()) {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("userId", sellerId)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Seller profile unavailable", Toast.LENGTH_SHORT).show()
                }
            }

            binding.buttonMessageSeller.setOnClickListener {
                if (currentUser == null) {
                    Toast.makeText(this, "Please log in to send messages", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    return@setOnClickListener
                }

                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("selectedAd", selectedAd)
                startActivity(intent)
            }
            
            // Show rate button for other users' ads
            binding.buttonRateSeller.visibility = View.VISIBLE
            binding.buttonRateSeller.setOnClickListener {
                val intent = Intent(this, AddReviewActivity::class.java)
                intent.putExtra("adId", selectedAd.id)
                intent.putExtra("sellerId", selectedAd.sellerId)
                startActivity(intent)
            }
        }
    }

    private fun configViewBinding() {
        binding = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}