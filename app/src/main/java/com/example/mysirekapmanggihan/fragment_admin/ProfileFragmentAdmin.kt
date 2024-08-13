package com.example.mysirekapmanggihan.fragment_admin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mysirekapmanggihan.LoginActivity
import com.example.mysirekapmanggihan.R
import com.example.mysirekapmanggihan.data.Contacts
import com.example.mysirekapmanggihan.databinding.FragmentProfileAdminBinding
import com.example.mysirekapmanggihan.databinding.FragmentProfileBinding
import com.example.mysirekapmanggihan.preference.Preferences
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileFragmentAdmin : Fragment() {

    private var _binding: FragmentProfileAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: Preferences
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileAdminBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = Preferences(requireContext())

        // Initialize Firebase reference
        firebaseRef = FirebaseDatabase.getInstance().getReference("users")

        binding.tvSend.setOnClickListener {
            saveData()
        }

        binding.cvAdminLogout.setOnClickListener {
            preferences.prefClear()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun saveData() {
        val address = binding.etAddress.text.toString()
        val date = binding.etDate.text.toString()
        val phone = binding.etPhone.text.toString()
        val name = binding.etName.text.toString()
        val jeniKelamin = binding.etKelamin.text.toString()
        val level = binding.etLevel.text.toString()
        val password = binding.etPassword.text.toString()

        if (address.isEmpty()) binding.etAddress.error = "Silahkan tulis alamat anda"
        if (date.isEmpty()) binding.etDate.error = "Silahkan tulis tanggal"
        if (phone.isEmpty()) binding.etPhone.error = "Silahkan tulis nomor telepon"
        if (name.isEmpty()) binding.etName.error = "Silahkan tulis nama anda"
        if (jeniKelamin.isEmpty()) binding.etKelamin.error = "Silahkan tulis jenis kelamin anda"
        if (level.isEmpty()) binding.etLevel.error = "Silahkan tulis level anda"
        if (password.isEmpty()) binding.etPassword.error = "Silahkan tulis password anda"

        val contanctId = firebaseRef.push().key.toString()
        val contacts = Contacts(address, date, name, jeniKelamin, level, password, phone)

        firebaseRef.child(contanctId).setValue(contacts)
            .addOnCompleteListener {
                Toast.makeText(requireContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Data gagal disimpan ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}