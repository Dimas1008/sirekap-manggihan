package com.example.mysirekapmanggihan

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mysirekapmanggihan.databinding.ActivityDataSampahBinding
import com.example.mysirekapmanggihan.preference.Preferences
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DataSampahActivity : AppCompatActivity() {

    lateinit var binding: ActivityDataSampahBinding
    private lateinit var preferences: Preferences
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportActionBar()?.hide()

        // Initialize preferences first
        preferences = Preferences(this)

        // Set up view binding
        binding = ActivityDataSampahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("sampah")

        // Retrieve waste data if phone number is available in preferences
        preferences.prefPhone?.let {
            retrieveWasteData(it)
        }
    }

    private fun retrieveWasteData(phoneNumber: String) {
        databaseRef.orderByChild("phoneNumber").equalTo(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val beratOrganikPerBulan = mutableMapOf<String, Float>()
                    val beratNonOrganikPerBulan = mutableMapOf<String, Float>()
                    val logamPerBulan = mutableMapOf<String, Float>()

                    // Process each data entry
                    for (dataSnapshot in snapshot.children) {
                        val berat = dataSnapshot.child("berat").getValue(Int::class.java)
                        val tanggal = dataSnapshot.child("tanggal").getValue(String::class.java)
                        val jenis = dataSnapshot.child("jenis").getValue(String::class.java)

                        Log.d("FirebaseData", "Berat: $berat, Tanggal: $tanggal, Jenis: $jenis")

                        if (berat != null && tanggal != null && jenis != null) {
                            val month = getMonthFromDate(tanggal)
                            when (jenis) {
                                "Kaca" -> beratOrganikPerBulan[month] =
                                    beratOrganikPerBulan.getOrDefault(month, 0f) + berat
                                "Plastik" -> beratNonOrganikPerBulan[month] =
                                    beratNonOrganikPerBulan.getOrDefault(month, 0f) + berat
                                "Logam" -> logamPerBulan[month] =
                                    logamPerBulan.getOrDefault(month, 0f) + berat
                            }
                        }
                    }

                    // Display the chart with retrieved data
                    displayChart(beratOrganikPerBulan, beratNonOrganikPerBulan, logamPerBulan)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DataSampahActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun displayChart(
        beratOrganikPerBulan: Map<String, Float>,
        beratNonOrganikPerBulan: Map<String, Float>,
        logamPerBulan: Map<String, Float>
    ) {
        val organikEntries = arrayListOf<BarEntry>()
        val nonOrganikEntries = arrayListOf<BarEntry>()
        val logamEntries = arrayListOf<BarEntry>()
        val labels = arrayListOf<String>()
        var index = 0.0f // Start index at 0 for better separation
        val allMonths = beratOrganikPerBulan.keys.union(beratNonOrganikPerBulan.keys)
            .union(logamPerBulan.keys).sorted()

        for (month in allMonths) {
            organikEntries.add(BarEntry(index, beratOrganikPerBulan[month] ?: 0f))
            nonOrganikEntries.add(BarEntry(index + 0.2f, beratNonOrganikPerBulan[month] ?: 0f))
            logamEntries.add(BarEntry(index + 0.4f, logamPerBulan[month] ?: 0f))
            labels.add(month)
            index += 1f // Increment index for next group of bars
        }

        val organikDataSet = BarDataSet(organikEntries, "Kaca").apply {
            color = Color.GREEN
            valueTextColor = Color.BLACK
            valueTextSize = 14f
            valueFormatter = KgValueFormatter()
        }

        val nonOrganikDataSet = BarDataSet(nonOrganikEntries, "Plastik").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
            valueTextSize = 14f
            valueFormatter = KgValueFormatter()
        }

        val logamDataSet = BarDataSet(logamEntries, "Logam").apply {
            color = Color.YELLOW
            valueTextColor = Color.BLACK
            valueTextSize = 14f
            valueFormatter = KgValueFormatter()
        }

        val data = BarData(organikDataSet, nonOrganikDataSet, logamDataSet).apply {
            barWidth = 0.15f // Set bar width
        }

        binding.barChartSampah.apply {
            this.data = data
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textSize = 12f
                setDrawGridLines(false)
                labelRotationAngle = 0f // Keep labels horizontal
            }
            axisLeft.apply {
                axisMinimum = 0f
                textSize = 12f
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.textSize = 12f
            setFitBars(true)

            // Group bars with appropriate spacing
            val groupSpace = 0.4f
            val barSpace = 0.05f // Space between bars within a group
            val barWidth = 0.15f // Width of each bar

            data.barWidth = barWidth
            this.groupBars(0f, groupSpace, barSpace)

            xAxis.axisMinimum = 0f // Start the X-axis from 0
            xAxis.axisMaximum = labels.size.toFloat() // End the X-axis at the last label
            xAxis.setCenterAxisLabels(true) // Center the labels under the groups

            animateY(1000)
            invalidate()
        }
    }

    // Function to extract month and year from date string
    private fun getMonthFromDate(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
            val date = sdf.parse(dateString)
            date?.let {
                val calendar = Calendar.getInstance()
                calendar.time = date
                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                return monthFormat.format(calendar.time)
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // Custom ValueFormatter to append "kg" to values
    private class KgValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return String.format("%.1f kg", value) // Format with one decimal place and append "kg"
        }
    }
}