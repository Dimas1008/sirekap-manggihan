package com.example.mysirekapmanggihan.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mysirekapmanggihan.fragment_admin.ChartFragmentAdmin
import com.example.mysirekapmanggihan.fragment_admin.HomeFragmentAdmin
import com.example.mysirekapmanggihan.LoginActivity
import com.example.mysirekapmanggihan.fragment_admin.ProfileFragmentAdmin
import com.example.mysirekapmanggihan.R
import com.example.mysirekapmanggihan.fragment_admin.RiwayatFragmentAdmin
import com.example.mysirekapmanggihan.databinding.ActivityAdminBinding
import com.example.mysirekapmanggihan.preference.Preferences
import com.google.firebase.database.DatabaseReference

class AdminActivity : AppCompatActivity() {

    lateinit var binding: ActivityAdminBinding
    private lateinit var firebaseRef: DatabaseReference

    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        getSupportActionBar()?.hide()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = Preferences(this)

        // Memeriksa status login
        if (!preferences.prefStatus) {
            // Jika belum login, pindah ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Mengganti fragment dengan HomeFragment sebagai tampilan awal
        replaceFragment(HomeFragmentAdmin())

        // Mengatur listener untuk item yang dipilih di navigation bar
        binding.navViewBottomAdmin.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home_admin -> replaceFragment(HomeFragmentAdmin())
                R.id.menu_riwayat_admin -> replaceFragment(RiwayatFragmentAdmin())
                R.id.menu_chart_admin -> replaceFragment(ChartFragmentAdmin())
                R.id.menu_profile_admin -> replaceFragment(ProfileFragmentAdmin())
                else -> {
                }
            }
            true
        }
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    // Fungsi untuk mengganti fragment yang ditampilkan
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}