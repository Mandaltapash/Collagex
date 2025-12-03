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
                binding.textTitle.text = ad.title
                binding.textDescription.text = ad.description
                binding.textValue.text = ad.value
                binding.textCollegeName.text = ad.collegeName
                val urlImages: List<String> = ad.adImages
                val urlCover: String = urlImages[0]
                Picasso.get().load(urlCover).into(binding.imageAd)
            }
        }
    }

    override fun getItemCount(): Int {
        return adList.size
    }
}