package com.example.mysirekapmanggihan

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mysirekapmanggihan.databinding.ActivitySplashBinding
import com.example.mysirekapmanggihan.ui.AdminActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_TIMEOUT = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        getSupportActionBar()?.hide()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Handler().postDelayed({
            checkUserRole()
        }, SPLASH_TIMEOUT.toLong())
    }

    private fun checkUserRole() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val databaseRef = FirebaseDatabase.getInstance().getReference("users")
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.child("phone").getValue(String::class.java)
                    if (role == "admin") {
                        val adminIntent = Intent(this@SplashActivity, AdminActivity::class.java)
                        startActivity(adminIntent)
                    } else {
                        val userIntent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(userIntent)
                    }
                    finish()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SplashActivity, "Failed to load user role", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            val loginIntent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
    }
}
