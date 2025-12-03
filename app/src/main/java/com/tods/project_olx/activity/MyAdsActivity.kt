package com.tods.project_olx.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.tods.project_olx.adapter.AdapterAd
import com.tods.project_olx.databinding.ActivityMyAdsBinding
import com.tods.project_olx.helper.RecyclerItemClickListener
import com.tods.project_olx.model.Ad
import com.tods.project_olx.model.User
import dmax.dialog.SpotsDialog
import java.util.*
import kotlin.collections.ArrayList

class MyAdsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyAdsBinding
    private lateinit var recyclerMyAds: RecyclerView
    private lateinit var adUserRef: DatabaseReference
    private lateinit var dialog: android.app.AlertDialog
    private var ads: MutableList<Ad> = ArrayList<Ad>()
    private var adapterAd: AdapterAd = AdapterAd(ads)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configToolbar()
        configViewBinding()
        configFabNewAd()
        configRecyclerView()
        recoverAds()
    }

    private fun recoverAds(){
        configDialog()
        adUserRef = FirebaseDatabase.getInstance()
            .getReference("my_adds")
            .child(User().configCurrentUser()!!.uid.toString())
        adUserRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                ads.clear()
                for (ds: DataSnapshot in snapshot.children){
                    ads.add(ds.getValue(Ad::class.java)!!)
                }
                ads.reverse()
                adapterAd.notifyDataSetChanged()
                dialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun configDialog() {
        dialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Recovering ads")
            .setCancelable(false)
            .build()
        dialog.show()
    }

    private fun configRecyclerView() {
        recyclerMyAds = binding.recyclerMyAds
        recyclerMyAds.layoutManager = LinearLayoutManager(this)
        recyclerMyAds.setHasFixedSize(true)
        recyclerMyAds.adapter = adapterAd
        recyclerMyAds.addOnItemTouchListener(RecyclerItemClickListener
            (this, recyclerMyAds, object: RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View, position: Int) {
                val selectedAd: Ad = ads[position]
                val intent = Intent(applicationContext, AdDetailsActivity::class.java)
                intent.putExtra("selectedAd", selectedAd)
                startActivity(intent)
            }

            override fun onItemLongClick(view: View?, position: Int) {
                val dialog: AlertDialog.Builder = AlertDialog.Builder(view!!.context)
                dialog.setTitle("Do you want to remove this ad?")
                dialog.setPositiveButton("Yes"){ _, _ ->
                    val selectedAd: Ad = ads[position]
                    selectedAd.remove()
                }
                dialog.setNegativeButton("Cancel"){ _, _ ->

                }
                val executeDialog: Dialog = dialog.create()
                executeDialog.show()
            }
        }))
    }

    private fun configToolbar() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "My Ads"
    }

    private fun configFabNewAd() {
        binding.fabNewAdd.setOnClickListener(View.OnClickListener {
            val intent: Intent = Intent(applicationContext, RegisterAddActivity::class.java)
            startActivity(intent)
        })
    }

    private fun configViewBinding() {
        binding = ActivityMyAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}