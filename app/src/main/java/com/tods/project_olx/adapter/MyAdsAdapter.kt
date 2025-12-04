package com.tods.project_olx.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tods.project_olx.activity.EditAdActivity
import com.tods.project_olx.databinding.CustomMyAdItemBinding
import com.tods.project_olx.model.Ad

class MyAdsAdapter(private val adList: List<Ad>)
    : RecyclerView.Adapter<MyAdsAdapter.MyAdViewHolder>() {

    inner class MyAdViewHolder(val binding: CustomMyAdItemBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdViewHolder {
        val binding = CustomMyAdItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyAdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyAdViewHolder, position: Int) {
        with(holder) {
            with(adList[position]) {
                val ad: Ad = adList[position]

                // Set product title
                binding.textProductTitle.text = ad.title

                // Set price only (no stock info)
                binding.textPriceStock.text = ad.value

                // Load product image
                val urlImages: List<String> = ad.adImages
                if (urlImages.isNotEmpty()) {
                    val urlCover: String = urlImages[0]
                    Picasso.get()
                        .load(urlCover)
                        .fit()
                        .centerCrop()
                        .into(binding.imageProduct)
                }

                // Edit button click listener
                binding.buttonEdit.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, EditAdActivity::class.java)
                    intent.putExtra("adToEdit", ad)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return adList.size
    }
}
