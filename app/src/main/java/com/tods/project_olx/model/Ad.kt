package com.tods.project_olx.model

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable

data class Ad(
    var id: String = "",
    var category: String = "",
    var title: String = "",
    var description: String = "",
    var value: String = "",
    var adImages: List<String> = mutableListOf(),
    var sellerId: String = "",
    var buyerId: String? = null,
    var status: String = "available",
    var price: Long = 0L,
    var createdAt: Long = 0L,
    var location: String = "",
    var ratingAverage: Double = 0.0,
    var ratingCount: Int = 0,
    var sellerName: String = "",
    var district: String = "", // New field
    var collegeName: String = "" // New field
): Serializable{
    private lateinit var database: DatabaseReference

    fun ensureSellerAndTimestamps(){
        if (sellerId.isEmpty()){
            val currentUser = User().configCurrentUser()
            if (currentUser != null){
                sellerId = currentUser.uid
            }
        }
        if (createdAt == 0L){
            createdAt = System.currentTimeMillis()
        }
    }

    fun save(){
        ensureSellerAndTimestamps()

        // Save under user-specific node
        database = FirebaseDatabase.getInstance().getReference("my_adds")
            .child(User().configCurrentUser()!!.uid.toString())
            .child(id)
        database.setValue(this)

        // Save under existing public tree used by current app
        savePublicByRegionAndCategory()

        // Save under flat listing for home feed and generic queries
        savePublicFlat()
    }

    private fun savePublicByRegionAndCategory(){
        database = FirebaseDatabase.getInstance().getReference("adds")
            .child(district)
            .child(category)
            .child(id)
        database.setValue(this)
    }

    private fun savePublicFlat(){
        database = FirebaseDatabase.getInstance().getReference("ads_all")
            .child(id)
        database.setValue(this)

        // Index by seller for profile/my listings views
        if (sellerId.isNotEmpty()){
            val sellerRef = FirebaseDatabase.getInstance().getReference("adsBySeller")
                .child(sellerId)
                .child(id)
            sellerRef.setValue(true)
        }
    }

    fun update() {
        // Update under user-specific node
        database = FirebaseDatabase.getInstance().getReference("my_adds")
            .child(User().configCurrentUser()!!.uid.toString())
            .child(id)
        database.setValue(this)

        // Update under existing public tree used by current app
        updatePublicByRegionAndCategory()

        // Update under flat listing for home feed and generic queries
        updatePublicFlat()
    }

    private fun updatePublicByRegionAndCategory() {
        database = FirebaseDatabase.getInstance().getReference("adds")
            .child(district)
            .child(category)
            .child(id)
        database.setValue(this)
    }

    private fun updatePublicFlat() {
        database = FirebaseDatabase.getInstance().getReference("ads_all")
            .child(id)
        database.setValue(this)

        // Update index by seller for profile/my listings views
        if (sellerId.isNotEmpty()) {
            val sellerRef = FirebaseDatabase.getInstance().getReference("adsBySeller")
                .child(sellerId)
                .child(id)
            sellerRef.setValue(true) // Set it again to update
        }
    }

    fun remove(){
        // Remove from user-specific node
        database = FirebaseDatabase.getInstance().getReference("my_adds")
            .child(User().configCurrentUser()!!.uid.toString())
            .child(id)
        database.removeValue()

        // Remove from existing public tree
        removePublicByRegionAndCategory()

        // Remove from flat listing and seller index
        removePublicFlat()
    }

    private fun removePublicByRegionAndCategory(){
        database = FirebaseDatabase.getInstance().getReference("adds")
            .child(district)
            .child(category)
            .child(id)
        database.removeValue()
    }

    private fun removePublicFlat(){
        database = FirebaseDatabase.getInstance().getReference("ads_all")
            .child(id)
        database.removeValue()

        if (sellerId.isNotEmpty()){
            val sellerRef = FirebaseDatabase.getInstance().getReference("adsBySeller")
                .child(sellerId)
                .child(id)
            sellerRef.removeValue()
        }
    }
}
