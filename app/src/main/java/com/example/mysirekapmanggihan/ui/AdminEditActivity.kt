package com.example.mysirekapmanggihan.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mysirekapmanggihan.R
import com.example.mysirekapmanggihan.databinding.ActivityAdminEditBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar


class AdminEditActivity : AppCompatActivity() {

    lateinit var binding: ActivityAdminEditBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()
        binding = ActivityAdminEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        // Isi dropdown dusun
        val jenisDusun = listOf("Dusun 1", "Dusun 2", "Dusun 3")
        val adapterDusun =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jenisDusun)
        binding.itemEditDusun.setAdapter(adapterDusun)

        // Isi dropdown jenis sampah
        val jenisSampah = listOf("Divalidasi", "Proses")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jenisSampah)
        binding.itemEditAuto.setAdapter(adapter)

        // Set up radio group listener
        binding.materialRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.editTextLainnya.visibility = if (checkedId == R.id.radioLainnya) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        // Tanggal Picker
        binding.etEditTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog =
                DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                    binding.etEditTanggal.setText("$selectedDay-${selectedMonth + 1}-$selectedYear")
                }, year, month, day)
            datePickerDialog.show()
        }

        val itemId = intent.getStringExtra("sampah")
        if (itemId != null) {
            loadItemData(itemId)
        }

        // Aksi tombol simpan
        binding.cvSimpan.setOnClickListener {
            if (itemId != null) {
                saveItemData(itemId)
            }
        }

        // Aksi tombol batal
        binding.cvBatal.setOnClickListener {
            finish()
        }
    }

    private fun loadItemData(itemId: String) {
        database.child("sampah").child(itemId).get().addOnSuccessListener { dataSnapshot ->
            val nama = dataSnapshot.child("nama").value.toString()
            val dusun = dataSnapshot.child("dusun").value.toString()
            val alamat = dataSnapshot.child("alamat").value.toString()
            val berat = dataSnapshot.child("berat").value.toString()
            val jenis = dataSnapshot.child("jenis").value.toString()
            val number = dataSnapshot.child("phoneNumber").value.toString()
            val tanggal = dataSnapshot.child("tanggal").value.toString()
            val imageUrl = dataSnapshot.child("imageUrl").value.toString()
            val status = dataSnapshot.child("status").value.toString()

            binding.etEditNama.setText(nama)
            binding.itemEditDusun.setText(dusun, false)
            binding.etEditAlamat.setText(alamat)
            binding.etEditKg.setText(berat)

            // Set the checked radio button based on jenis value
            when (jenis) {
                "Plastik" -> binding.radioPlastik.isChecked = true
                "Logam" -> binding.radioLogam.isChecked = true
                "Kaca" -> binding.radioBotol.isChecked = true
                "Lainnya" -> binding.radioLainnya.isChecked = true
                // Add more cases as needed
                else -> {
                    binding.radioLainnya.isChecked = true
                    binding.editTextLainnya.setText(jenis)
                }
            }

            binding.etEditAdminPhone.setText(number)
            binding.itemEditAuto.setText(status, false)
            binding.etEditTanggal.setText(tanggal)
            Glide.with(this).load(imageUrl).into(binding.ivEditImage)
        }
    }


    private fun saveItemData(itemId: String) {
        binding.progresLoading.visibility = View.VISIBLE
        binding.linierLoading.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.tvLoading.visibility = View.VISIBLE

        val nama = binding.etEditNama.text.toString()
        val dusun = binding.itemEditDusun.text.toString()
        val alamat = binding.etEditAlamat.text.toString()
        val number = binding.etEditAdminPhone.text.toString()
        val berat = binding.etEditKg.text.toString()
        val status = binding.itemEditAuto.text.toString()
        val tanggal = binding.etEditTanggal.text.toString()

        val selectedMaterial: String = when (binding.materialRadioGroup.checkedRadioButtonId) {
            R.id.radioLainnya -> binding.editTextLainnya.text.toString()
            else -> {
                val selectedRadioButton =
                    findViewById<RadioButton>(binding.materialRadioGroup.checkedRadioButtonId)
                selectedRadioButton.text.toString()
            }
        }

        val item = mapOf(
            "nama" to nama,
            "dusun" to dusun,
            "jenis" to selectedMaterial,
            "alamat" to alamat,
            "phoneNumber" to number,
            "berat" to berat.toInt(),
            "tanggal" to tanggal,
            "status" to status
        )

        database.child("sampah").child(itemId).updateChildren(item).addOnSuccessListener {
            binding.progresLoading.visibility = View.GONE
            binding.linierLoading.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.tvLoading.visibility = View.GONE
            finish()
        }.addOnFailureListener { exception ->
            binding.progresLoading.visibility = View.GONE
            binding.linierLoading.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.tvLoading.visibility = View.GONE
            // Handle failure
            Log.d("Error", "Gagal Untuk Mengupdate Data", exception)
        }
    }
}
