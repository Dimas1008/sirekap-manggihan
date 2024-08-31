package com.example.mysirekapmanggihan.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mysirekapmanggihan.R
import com.example.mysirekapmanggihan.TambahDataActivity
import com.example.mysirekapmanggihan.adapter.AdminRiwayatAdapater
import com.example.mysirekapmanggihan.databinding.ActivityAdminTambahDataBinding
import com.example.mysirekapmanggihan.preference.Preferences
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AdminTambahDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminTambahDataBinding
    private lateinit var adapter: AdminRiwayatAdapater
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var preferences: Preferences
    private var imageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_tambah_data)
        getSupportActionBar()?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityAdminTambahDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Isi dropdown dusun
        val jenisSampah = listOf("Dusun 1", "Dusun 2", "Dusun 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jenisSampah)
        binding.itemEditAuto.setAdapter(adapter)

        firebaseRef = FirebaseDatabase.getInstance().getReference("sampah")
        preferences = Preferences(this)

        // Set up radio group listener
        binding.materialRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.editTextLainnya.visibility = if (checkedId == R.id.radioLainnya) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        binding.etAdminTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        binding.ivAdminExit.setOnClickListener {
            finish()
        }

        binding.btnKirim.setOnClickListener {
            submitData()
        }

        binding.tvImageAdmin.setOnClickListener {
            selectImageFromGallery()
        }

        binding.tvCameraAdmin.setOnClickListener {
            // Check camera permission before opening the camera
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
                )
            } else {
                takePhotoFromCamera()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Use the same date format as in HomeFragment
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = dateFormat.format(Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time)
                binding.etAdminTanggal.setText(selectedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun submitData() {
        val selectedMaterial: String = when (binding.materialRadioGroup.checkedRadioButtonId) {
            R.id.radioLainnya -> binding.editTextLainnya.text.toString().trim()
            else -> {
                val selectedRadioButton =
                    findViewById<RadioButton>(binding.materialRadioGroup.checkedRadioButtonId)
                selectedRadioButton.text.toString()
            }
        }

        val berat = binding.etAdminKg.text.toString().trim()
        val tanggal = binding.etAdminTanggal.text.toString().trim()
        val phoneNumber = binding.etAdminPhone.text.toString().trim()
        val nama = binding.etAdminNama.text.toString().trim()
        val alamat = binding.etAdminAlamat.text.toString().trim()
        val dusun = binding.itemEditAuto.text.toString().trim()

        if (selectedMaterial.isEmpty() || berat.isEmpty() || tanggal.isEmpty() || nama.isEmpty() || alamat.isEmpty() || dusun.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(this, "Harap pilih atau ambil gambar", Toast.LENGTH_SHORT).show()
            return
        }

        uploadData(selectedMaterial, berat, tanggal, phoneNumber, nama, alamat, dusun)
    }

    private fun uploadData(
        jenisSampah: String,
        berat: String,
        tanggal: String,
        phoneNumber: String,
        nama: String,
        alamat: String,
        dusun: String
    ) {
        binding.progresLoading.visibility = View.VISIBLE
        binding.linierLoading.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.tvLoading.visibility = View.VISIBLE


        val fileName = UUID.randomUUID().toString() + ".jpg"
        val imageRef = storageRef.reference.child("images/$fileName")

        imageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    val sampah = mapOf(
                        "jenis" to jenisSampah,
                        "berat" to berat.toInt(),
                        "tanggal" to tanggal,
                        "status" to "Divalidasi",
                        "imageUrl" to imageUrl,
                        "phoneNumber" to phoneNumber,
                        "nama" to nama,
                        "alamat" to alamat,
                        "dusun" to dusun
                    )

                    firebaseRef.push().setValue(sampah)
                        .addOnSuccessListener {
                            binding.progresLoading.visibility = View.GONE
                            binding.linierLoading.visibility = View.GONE
                            binding.progressBar.visibility = View.GONE
                            binding.tvLoading.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "Data sampah berhasil ditambahkan",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            binding.progresLoading.visibility = View.GONE
                            binding.linierLoading.visibility = View.GONE
                            binding.progressBar.visibility = View.GONE
                            binding.tvLoading.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "Gagal menambahkan data sampah: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                binding.progresLoading.visibility = View.GONE
                binding.linierLoading.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.tvLoading.visibility = View.GONE
                Toast.makeText(this, "Gagal mengunggah gambar: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_GALLERY -> {
                    imageUri = data?.data
                    binding.ivImageAdmin.setImageURI(imageUri)
                }

                REQUEST_IMAGE_CAMERA -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    imageUri = getImageUriFromBitmap(bitmap)
                    binding.ivImageAdmin.setImageURI(imageUri)
                }
            }
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            UUID.randomUUID().toString(),
            null
        )
        return Uri.parse(path)
    }

    companion object {
        private const val REQUEST_IMAGE_GALLERY = 100
        private const val REQUEST_IMAGE_CAMERA = 101
        private const val CAMERA_PERMISSION_CODE = 102
    }
}
