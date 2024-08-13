package com.example.mysirekapmanggihan.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysirekapmanggihan.R
import com.example.mysirekapmanggihan.TambahDataActivity
import com.example.mysirekapmanggihan.adapter.AdminRiwayatAdapater
import com.example.mysirekapmanggihan.adapter.RiwayatAdapter
import com.example.mysirekapmanggihan.data.Sampah
import com.example.mysirekapmanggihan.databinding.FragmentRiwayatBinding
import com.example.mysirekapmanggihan.preference.Preferences
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RiwayatFragment : Fragment() {

    private lateinit var binding: FragmentRiwayatBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var preferences: Preferences
    private lateinit var adapter: RiwayatAdapter
    private val sampahList = mutableListOf<Sampah>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inisialisasi Firebase dan preferensi
        databaseRef = FirebaseDatabase.getInstance().getReference("sampah")
        preferences = Preferences(requireContext())

        // Mengatur RecyclerView
        binding.rvRiwayat.layoutManager = LinearLayoutManager(context)
        adapter = RiwayatAdapter(sampahList)
        binding.rvRiwayat.adapter = adapter

        // Mengatur click listener untuk CardView
        binding.cvTambahSampah.setOnClickListener {
            // Start TambahDataActivity
            val intent = Intent(activity, TambahDataActivity::class.java)
            startActivity(intent)
        }

        // Add refresh button click listener
        binding.cvRefresh.setOnClickListener {
            refreshPage()
        }

        // Mengambil data dari Firebase
        fetchDataFromFirebase()

        return view
    }

    // Fungsi untuk mengambil data dari Firebase
    private fun fetchDataFromFirebase() {
        val phoneNumber = preferences.prefPhone ?: return

        databaseRef.orderByChild("phoneNumber").equalTo(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sampahList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val sampah = dataSnapshot.getValue(Sampah::class.java)
                        sampah?.let { sampahList.add(it) }
                    }
                    if (sampahList.isEmpty()) {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.rvRiwayat.visibility = View.GONE
                    } else {
                        binding.tvNoData.visibility = View.GONE
                        binding.rvRiwayat.visibility = View.VISIBLE
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Method to refresh the page
    private fun refreshPage() {
        // Clear the current list and fetch data again
        sampahList.clear()
        fetchDataFromFirebase()
        Toast.makeText(context, "Halaman diperbarui", Toast.LENGTH_SHORT).show()
    }
}