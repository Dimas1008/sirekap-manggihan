package com.example.mysirekapmanggihan.fragment_admin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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

        // Isi dropdown dusun
        val jenisSampah = listOf("Dusun 1", "Dusun 2", "Dusun 3")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, jenisSampah)
        binding.itemEditAuto.setAdapter(adapter)

        val jenisLevel = listOf("admin", "user")
        val adapterLevel = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, jenisLevel)
        binding.itemEditLevel.setAdapter(adapterLevel)

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
        val dusun = binding.itemEditAuto.text.toString()
        val address = binding.etAddress.text.toString()
        val phone = binding.etPhone.text.toString()
        val name = binding.etName.text.toString()
        val level = binding.itemEditLevel.text.toString()
        val password = binding.etPassword.text.toString()

        // Check for empty fields
        if (dusun.isEmpty()) {
            binding.itemDusun.error = "Silahkan tulis nama dusun"
            return
        }
        if (address.isEmpty()) {
            binding.etAddress.error = "Silahkan tulis alamat anda"
            return
        }
        if (phone.isEmpty()) {
            binding.etPhone.error = "Silahkan tulis nomor telepon"
            return
        }
        if (name.isEmpty()) {
            binding.etName.error = "Silahkan tulis nama anda"
            return
        }
        if (level.isEmpty()) {
            binding.itemLevel.error = "Silahkan tulis level anda"
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Silahkan tulis password anda"
            return
        }

        val contactId = firebaseRef.push().key.toString()
        val contacts = Contacts(dusun, address, name, level, password, phone)

        firebaseRef.child(contactId).setValue(contacts)
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