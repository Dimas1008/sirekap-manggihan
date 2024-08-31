package com.example.mysirekapmanggihan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView
import com.example.mysirekapmanggihan.data.Sampah
import com.example.mysirekapmanggihan.databinding.ItemRiwayatSampahBinding

// Adapter untuk RecyclerView yang menampilkan daftar riwayat Sampah
class RiwayatAdapter(private val sampahList: List<Sampah>) :
    RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder>() {

    class RiwayatViewHolder(private val binding: ItemRiwayatSampahBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Fungsi untuk menampilkan data riwayat Sampah ke tampilan
        fun bind(sampah: Sampah) {
            binding.tvNamaPengirim.text = sampah.nama
            binding.tvJenisSampah.text = sampah.jenis
            binding.tvTanggal.text = sampah.tanggal
            binding.tvBerat.text = sampah.berat.toString()
            binding.tvProses.text = sampah.status
            binding.tvAlamat.text = sampah.alamat
            binding.tvDusun.text = sampah.dusun

            // Menggunakan Glide untuk memuat gambar dari URL ke ImageView
            Glide.with(binding.ivRiwayatSampah.context).load(sampah.imageUrl).into(binding.ivRiwayatSampah)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val binding = ItemRiwayatSampahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RiwayatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        holder.bind(sampahList[position])
    }

    override fun getItemCount(): Int {
        return sampahList.size
    }
}