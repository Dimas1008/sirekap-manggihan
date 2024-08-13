package com.example.mysirekapmanggihan.fragment_admin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysirekapmanggihan.ui.AdminTambahDataActivity
import com.example.mysirekapmanggihan.adapter.AdminRiwayatAdapater
import com.example.mysirekapmanggihan.data.Sampah
import com.example.mysirekapmanggihan.databinding.FragmentRiwayatAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RiwayatFragmentAdmin : Fragment() {

    private lateinit var binding: FragmentRiwayatAdminBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var adapter: AdminRiwayatAdapater
    private val sampahList = mutableListOf<Sampah>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatAdminBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inisialisasi Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("sampah")

        // Mengatur RecyclerView
        binding.rvRiwayat.layoutManager = LinearLayoutManager(context)
        adapter = AdminRiwayatAdapater(sampahList)
        binding.rvRiwayat.adapter = adapter

        // Mengatur click listener untuk CardView
        binding.cvTambahSampah.setOnClickListener {
            // Start TambahDataActivity
            val intent = Intent(activity, AdminTambahDataActivity::class.java)
            startActivity(intent)
        }

        // Add refresh button click listener
        binding.cvRefresh.setOnClickListener {
            refreshPage()
        }

        // Mengambil data dari Firebase
        loadSampahData()

        // Set up SearchView
        binding.searchHome.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        return view
    }

    private fun loadSampahData() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                sampahList.clear()
                for (sampahSnapshot in snapshot.children) {
                    val sampah = sampahSnapshot.getValue(Sampah::class.java)
                    sampah?.let {
                        it.id = sampahSnapshot.key ?: "" // Tambahkan id dari key Firebase
                        sampahList.add(it)
                    }
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
        loadSampahData()
        Toast.makeText(context, "Halaman diperbarui", Toast.LENGTH_SHORT).show()
    }
}
