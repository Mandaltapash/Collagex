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
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityAdDetailsBinding
import com.tods.project_olx.model.Ad
import com.tods.project_olx.model.User
import java.text.NumberFormat
import java.util.*

class AdDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdDetailsBinding
    private lateinit var selectedAd: Ad
    private var sellerUser: User? = null
    private lateinit var userRef: DatabaseReference
    private var currentImagePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide action bar for custom toolbar
        supportActionBar?.hide()
        
        configViewBinding()
        configAdDetails()
        configCarousel()
        configCustomToolbar()
        configBottomButtons()
    }

    private fun configCustomToolbar() {
        // Back button
        binding.buttonBack.setOnClickListener {
            finish()
        }

        // Share button
        binding.buttonShare.setOnClickListener {
            shareAd()
        }

        // Favorite button (UI only for now)
        binding.buttonFavorite.setOnClickListener {
            Toast.makeText(this, "Favorite feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Heart icon in price section
        binding.iconFavoriteHeart.setOnClickListener {
            Toast.makeText(this, "Favorite feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareAd() {
        val shareText = "${selectedAd.title}\n${selectedAd.value}\n\nShared from CollageX"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
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
        
        // Update page indicator
        binding.viewPagerImages.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentImagePosition = position
                updatePageIndicator()
            }
        })
        
        updatePageIndicator()
    }
    
    private fun updatePageIndicator() {
        val total = selectedAd.adImages.size
        binding.textPageIndicator.text = "${currentImagePosition + 1}/$total"
    }

    private fun configAdDetails() {
        selectedAd = intent.getSerializableExtra("selectedAd") as Ad
        
        // Format price with rupee symbol
        binding.textAdDetailValue.text = formatPrice(selectedAd.value)
        
        // Set title
        binding.textAdDetailTitle.text = selectedAd.title
        
        // Set location (use district or college name)
        val location = if (selectedAd.district.isNotEmpty()) {
            selectedAd.district.uppercase()
        } else if (selectedAd.location.isNotEmpty()) {
            selectedAd.location.uppercase()
        } else {
            "LOCATION NOT SET"
        }
        binding.textAdLocation.text = location
        
        // Set timestamp (show TODAY or days ago)
        binding.textAdTimestamp.text = getTimeAgo(selectedAd.createdAt)
        
        // Set description
        binding.textAdDetailDescription.text = selectedAd.description
        
        // Set brand in details section (use category for now)
        if (selectedAd.category.isNotEmpty()) {
            binding.textBrand.text = selectedAd.category.uppercase()
        }
    }
    
    private fun formatPrice(value: String): String {
        return if (value.startsWith("₹") || value.startsWith("Rs")) {
            value
        } else {
            "₹ $value"
        }
    }
    
    private fun getTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return "RECENTLY"
        
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days < 1 -> "TODAY"
            days < 2 -> "YESTERDAY"
            days < 7 -> "$days DAYS AGO"
            else -> "RECENTLY"
        }
    }

    private fun configBottomButtons() {
        val currentUser = User().configCurrentUser()
        
        // Chat Button
        binding.buttonChat.setOnClickListener {
            if (currentUser == null) {
                Toast.makeText(this, "Please log in to chat", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                return@setOnClickListener
            }
            
            if (selectedAd.sellerId == currentUser.uid) {
                Toast.makeText(this, "You cannot chat with yourself", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("selectedAd", selectedAd)
            startActivity(intent)
        }
        
        // Make Offer Button
        binding.buttonMakeOffer.setOnClickListener {
            if (currentUser == null) {
                Toast.makeText(this, "Please log in to make an offer", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                return@setOnClickListener
            }
            
            if (selectedAd.sellerId == currentUser.uid) {
                Toast.makeText(this, "You cannot make an offer on your own ad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // For now, show a toast. Later can implement offer dialog or screen
            Toast.makeText(this, "Make Offer feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Call Button - Disabled with "Coming Soon"
        binding.buttonCall.isEnabled = false
        binding.buttonCall.setOnClickListener {
            Toast.makeText(this, "Call feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configViewBinding() {
        binding = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}