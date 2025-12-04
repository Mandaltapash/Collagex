package com.tods.project_olx.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.core.Context
import com.squareup.picasso.Picasso
import com.tods.project_olx.databinding.CustomAdapterAdBinding
import com.tods.project_olx.model.Ad

class AdapterAd(private val adList: List<Ad>)
    :RecyclerView.Adapter<AdapterAd.AdViewHolder>(){

        inner class AdViewHolder(val binding: CustomAdapterAdBinding)
            :RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = CustomAdapterAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        with(holder){
            with(adList[position]){
                val ad: Ad = adList[position]
                
                // Set text content
                binding.textTitle.text = ad.title
                binding.textDescription.text = ad.description
                binding.textValue.text = ad.value
                binding.textCollegeName.text = if (ad.collegeName.isNotEmpty()) ad.collegeName else "Location"
                
                // Load image with rounded corners
                val urlImages: List<String> = ad.adImages
                if (urlImages.isNotEmpty()) {
                    val urlCover: String = urlImages[0]
                    Picasso.get()
                        .load(urlCover)
                        .fit()
                        .centerCrop()
                        .into(binding.imageAd)
                }
                
                // Show verified badge conditionally (can be based on seller rating or verification status)
                // For now, hide it - can be shown based on seller data later
                binding.iconVerified.visibility = android.view.View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return adList.size
    }
}