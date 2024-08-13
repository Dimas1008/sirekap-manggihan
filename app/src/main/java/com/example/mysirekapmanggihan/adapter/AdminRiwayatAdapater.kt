package com.example.mysirekapmanggihan.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysirekapmanggihan.R
import com.example.mysirekapmanggihan.data.Sampah
import com.example.mysirekapmanggihan.fragment_admin.RiwayatFragmentAdmin
import com.example.mysirekapmanggihan.ui.AdminEditActivity
import com.google.firebase.database.FirebaseDatabase

class AdminRiwayatAdapater(private var sampahList: List<Sampah>) :
    RecyclerView.Adapter<AdminRiwayatAdapater.AdminRiwayatViewHolder>(), Filterable {

    private var sampahListFiltered: List<Sampah> = sampahList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminRiwayatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_riwayat_sampah, parent, false)
        return AdminRiwayatViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminRiwayatViewHolder, position: Int) {
        val sampah = sampahListFiltered[position]
        holder.bind(sampah)
    }

    override fun getItemCount(): Int = sampahListFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                sampahListFiltered = if (charString.isEmpty()) sampahList else {
                    sampahList.filter {
                        it.nama.contains(charString, true) ||
                        it.status.contains(charString, true)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = sampahListFiltered
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                sampahListFiltered = results?.values as List<Sampah>
                notifyDataSetChanged()
            }
        }
    }

    class AdminRiwayatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_riwayatAdminSampah)
        private val namaTextView: TextView = itemView.findViewById(R.id.tvAdminNamaPengirim)
        private val tanggalTextView: TextView = itemView.findViewById(R.id.tvTanggalAdmin)
        private val beratTextView: TextView = itemView.findViewById(R.id.tvBeratAdmin)
        private val alamatTextView: TextView = itemView.findViewById(R.id.tvAlamatAdmin)
        private val jenisTextView: TextView = itemView.findViewById(R.id.tvAdminJenisSampah)
        private val statusTextView: TextView = itemView.findViewById(R.id.tvProsesAdmin)
        private val cvAdminEdit: CardView = itemView.findViewById(R.id.cvAdminEdit)
        private val cvAdminHapus: CardView = itemView.findViewById(R.id.cvAdminHapus)

        fun bind(sampah: Sampah) {
            tanggalTextView.text = sampah.tanggal
            beratTextView.text = "${sampah.berat}"
            jenisTextView.text = sampah.jenis
            statusTextView.text = sampah.status
            namaTextView.text = sampah.nama
            alamatTextView.text = sampah.alamat

            // Menggunakan Glide untuk memuat gambar dari URL
            Glide.with(itemView.context)
                .load(sampah.imageUrl)
                .into(imageView)

            // Menangani klik pada CardView untuk membuka AdminEditActivity dengan data Sampah
            cvAdminEdit.setOnClickListener {
                val intent = Intent(itemView.context, AdminEditActivity::class.java).apply {
                    putExtra("sampah", sampah.id) // Kirim id melalui Intent
                }
                itemView.context.startActivity(intent)
            }

            // Klik untuk menghapus
            cvAdminHapus.setOnClickListener {
                val dialog = AlertDialog.Builder(itemView.context)
                dialog.setTitle("Konfirmasi Hapus")
                dialog.setMessage("Apakah Anda yakin ingin menghapus item ini?")
                dialog.setPositiveButton("Hapus") { _, _ ->
                    hapusDataDariFirebase(sampah.id)
                }
                dialog.setNegativeButton("Batal", null)
                dialog.show()
            }
        }

        private fun hapusDataDariFirebase(id: String) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("sampah").child(id)
            databaseRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(itemView.context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(itemView.context, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

