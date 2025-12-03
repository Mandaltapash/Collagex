package com.tods.project_olx.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tods.project_olx.adapter.AdapterAd
import com.tods.project_olx.databinding.ActivityPurchaseHistoryBinding
import com.tods.project_olx.model.Ad
import com.tods.project_olx.model.User

class PurchaseHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPurchaseHistoryBinding
    private lateinit var adsRef: DatabaseReference
    private var ads: MutableList<Ad> = mutableListOf()
    private lateinit var adapterAd: AdapterAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Purchase History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapterAd = AdapterAd(ads)
        binding.recyclerPurchases.layoutManager = LinearLayoutManager(this)
        binding.recyclerPurchases.setHasFixedSize(true)
        binding.recyclerPurchases.adapter = adapterAd

        loadPurchases()
    }

    private fun loadPurchases() {
        val currentUser = User().configCurrentUser()
        if (currentUser == null) {
            // Handle not logged in case
            return
        }

        adsRef = FirebaseDatabase.getInstance().getReference("ads_all")
        adsRef.orderByChild("buyerId").equalTo(currentUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ads.clear()
                    for (ds in snapshot.children) {
                        val ad = ds.getValue(Ad::class.java)
                        if (ad != null) {
                            ads.add(ad)
                        }
                    }
                    ads.reverse()
                    adapterAd.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}
