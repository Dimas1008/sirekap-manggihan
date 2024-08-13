package com.example.mysirekapmanggihan

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mysirekapmanggihan.databinding.ActivityMainBinding
import com.example.mysirekapmanggihan.fragment.HomeFragment
import com.example.mysirekapmanggihan.fragment.ProfileFragment
import com.example.mysirekapmanggihan.fragment.RiwayatFragment
import com.example.mysirekapmanggihan.preference.Preferences
import com.google.firebase.database.DatabaseReference

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
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
        binding = ActivityMainBinding.inflate(layoutInflater)
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
        replaceFragment(HomeFragment())

        // Mengatur listener untuk item yang dipilih di navigation bar
        binding.navViewBottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> replaceFragment(HomeFragment())
                R.id.menu_camera -> replaceFragment(RiwayatFragment())
                R.id.menu_profile -> replaceFragment(ProfileFragment())
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