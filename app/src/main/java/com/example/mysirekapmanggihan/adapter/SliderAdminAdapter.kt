package com.example.mysirekapmanggihan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mysirekapmanggihan.R
import com.smarteist.autoimageslider.SliderViewAdapter

// Adapter untuk SliderView yang menampilkan daftar URL gambar
class SliderAdminAdapter(imageurl: ArrayList<String>) :
    SliderViewAdapter<SliderAdminAdapter.SliderAdminViewHolder>() {

    var imageUrl: ArrayList<String> = imageurl

    override fun getCount(): Int {
        return imageUrl.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderAdminViewHolder {
        var inflate: View =
            LayoutInflater.from(parent!!.context).inflate(R.layout.item_slider, null)
        return SliderAdminViewHolder(inflate)
    }

    // Mengikat data ke ViewHolder yang telah dibuat
    override fun onBindViewHolder(viewHolder: SliderAdminViewHolder?, position: Int) {
        if (viewHolder != null)
            Glide.with(viewHolder.itemView).load(imageUrl.get(position)).fitCenter()
                .into(viewHolder.imageView)
    }

    class SliderAdminViewHolder(itemView: View) : ViewHolder(itemView) {
        // ImageView untuk menampilkan gambar di slider
        val imageView: ImageView = itemView.findViewById(R.id.ivSlider)
    }
}

