package com.example.mysirekapmanggihan

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mysirekapmanggihan.databinding.ActivityTambahDataBinding
import com.example.mysirekapmanggihan.preference.Preferences
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TambahDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahDataBinding
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var databaseRef: DatabaseReference
    private lateinit var preferences: Preferences
    private var imageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.hide()

        firebaseRef = FirebaseDatabase.getInstance().getReference("sampah")
        databaseRef = FirebaseDatabase.getInstance().getReference("users")
        preferences = Preferences(this)

        // Retrieve and display user data
        preferences.prefPhone?.let {
            retrieveUserData(it)
        }

        // Set up radio group listener
        binding.materialRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.editTextLainnya.visibility = if (checkedId == R.id.radioLainnya) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        // Set up date picker dialog
        binding.etTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        // Set up exit button
        binding.ivExit.setOnClickListener {
            finish()
        }

        // Set up submit button
        binding.btnKirim.setOnClickListener {
            submitData()
        }

        // Set up image selection buttons
        binding.tvImage.setOnClickListener {
            selectImageFromGallery()
        }

        binding.tvCamera.setOnClickListener {
            // Check camera permission before opening the camera
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                takePhotoFromCamera()
            }
        }
    }

    private fun retrieveUserData(phoneNumber: String) {
        databaseRef.orderByChild("phone").equalTo(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val name = userSnapshot.child("name").getValue(String::class.java)
                        val address = userSnapshot.child("address").getValue(String::class.java)
                        val dusun = userSnapshot.child("dusun").getValue(String::class.java)

                        binding.etNama.setText(name ?: "No Name")
                        binding.etAlamat.setText(address ?: "No Address")
                        binding.etDusun.setText(dusun ?: "No Dusun")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TambahDataActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format the date as dd/MM/yyyy
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate = dateFormat.format(Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }.time)
            binding.etTanggal.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun submitData() {
        val selectedMaterial: String = when (binding.materialRadioGroup.checkedRadioButtonId) {
            R.id.radioLainnya -> binding.editTextLainnya.text.toString().trim()
            else -> {
                val selectedRadioButton = findViewById<RadioButton>(binding.materialRadioGroup.checkedRadioButtonId)
                selectedRadioButton.text.toString()
            }
        }

        val berat = binding.etKg.text.toString().trim()
        val tanggal = binding.etTanggal.text.toString().trim()
        val phoneNumber = preferences.prefPhone ?: return
        val nama = binding.etNama.text.toString().trim()
        val alamat = binding.etAlamat.text.toString().trim()
        val dusun = binding.etDusun.text.toString().trim()

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
                        "status" to "Proses",
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
                    binding.ivImage.setImageURI(imageUri)
                }

                REQUEST_IMAGE_CAMERA -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    imageUri = getImageUriFromBitmap(bitmap)
                    binding.ivImage.setImageURI(imageUri)
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


