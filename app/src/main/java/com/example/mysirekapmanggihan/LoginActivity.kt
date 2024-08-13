package com.example.mysirekapmanggihan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mysirekapmanggihan.databinding.ActivityLoginBinding
import com.example.mysirekapmanggihan.preference.Preferences
import com.example.mysirekapmanggihan.ui.AdminActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        getSupportActionBar()?.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseRef = FirebaseDatabase.getInstance().getReference("users")
        preferences = Preferences(this)

        // Check login status
        if (preferences.prefStatus) {
            navigateToNextActivity(preferences.prefLevel)
        }

        binding.btnLogin.setOnClickListener {
            val phoneNumber = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (phoneNumber.isNotEmpty() && password.isNotEmpty()) {
                performLogin(phoneNumber, password)
            } else {
                Toast.makeText(this, "Please enter both phone number and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogin(phoneNumber: String, password: String) {
        binding.progresLoading.visibility = View.VISIBLE
        binding.linierLoading.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.tvLoading.visibility = View.VISIBLE

        firebaseRef.orderByChild("phone").equalTo(phoneNumber).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val storedPassword = userSnapshot.child("password").getValue(String::class.java)
                        val userLevel = userSnapshot.child("level").getValue(String::class.java)
                        if (storedPassword == password) {
                            preferences.prefStatus = true
                            preferences.prefLevel = userLevel
                            preferences.prefPhone = phoneNumber

                            Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                            navigateToNextActivity(userLevel)
                            return
                        }
                    }
                    Toast.makeText(this@LoginActivity, "Incorrect password", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "User does not exist", Toast.LENGTH_SHORT).show()
                }
                binding.progresLoading.visibility = View.GONE
                binding.linierLoading.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.tvLoading.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Login failed: ${error.message}", Toast.LENGTH_SHORT).show()
                binding.progresLoading.visibility = View.GONE
                binding.linierLoading.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.tvLoading.visibility = View.GONE
            }
        })
    }

    private fun navigateToNextActivity(userLevel: String?) {
        val intent = when (userLevel) {
            "admin" -> Intent(this, AdminActivity::class.java)
            "user" -> Intent(this, MainActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
