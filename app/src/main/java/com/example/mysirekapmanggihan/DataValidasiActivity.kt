package com.example.mysirekapmanggihan

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mysirekapmanggihan.databinding.ActivityAdminBinding
import com.example.mysirekapmanggihan.databinding.ActivityDataValidasiBinding
import com.example.mysirekapmanggihan.preference.Preferences
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DataValidasiActivity : AppCompatActivity() {

    lateinit var binding: ActivityDataValidasiBinding
    private lateinit var preferences: Preferences
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportActionBar()?.hide()

        // Initialize preferences first
        preferences = Preferences(this)

        // Initialize view binding
        binding = ActivityDataValidasiBinding.inflate(layoutInflater)
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
                    val validatedDataPerBulan = mutableMapOf<String, Float>()
                    val processedDataPerBulan = mutableMapOf<String, Float>()

                    for (dataSnapshot in snapshot.children) {
                        val berat = dataSnapshot.child("berat").getValue(Int::class.java)
                        val tanggal = dataSnapshot.child("tanggal").getValue(String::class.java)
                        val jenis = dataSnapshot.child("jenis").getValue(String::class.java)
                        val status = dataSnapshot.child("status").getValue(String::class.java)

                        if (berat != null && tanggal != null && jenis != null && status != null) {
                            val month = getMonthFromDate(tanggal)
                            when (status) {
                                "Divalidasi" -> {
                                    validatedDataPerBulan[month] =
                                        validatedDataPerBulan.getOrDefault(month, 0f) + berat
                                }
                                "Proses" -> {
                                    processedDataPerBulan[month] =
                                        processedDataPerBulan.getOrDefault(month, 0f) + berat
                                }
                            }
                        }
                    }

                    updateBarChart(validatedDataPerBulan, processedDataPerBulan)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DataValidasiActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateBarChart(
        validatedDataPerBulan: Map<String, Float>,
        processedDataPerBulan: Map<String, Float>
    ) {
        val validatedEntries = arrayListOf<BarEntry>()
        val processedEntries = arrayListOf<BarEntry>()
        val labels = arrayListOf<String>()
        var index = 0.0f
        val allMonths = validatedDataPerBulan.keys.union(processedDataPerBulan.keys).sorted()

        for (month in allMonths) {
            validatedEntries.add(BarEntry(index, validatedDataPerBulan[month] ?: 0f))
            processedEntries.add(BarEntry(index + 0.2f, processedDataPerBulan[month] ?: 0f))
            labels.add(month)
            index += 1f
        }

        val validatedDataSet = BarDataSet(validatedEntries, "Divalidasi").apply {
            color = Color.GREEN
            valueTextColor = Color.BLACK
            valueTextSize = 14f
            valueFormatter = KgValueFormatter()
        }

        val processedDataSet = BarDataSet(processedEntries, "Diproses").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 14f
            valueFormatter = KgValueFormatter()
        }

        val data = BarData(validatedDataSet, processedDataSet).apply {
            barWidth = 0.3f // Mengatur lebar bar
        }

        binding.barChartValidasi.apply {
            this.data = data
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textSize = 12f
                setDrawGridLines(false)
                labelRotationAngle = 0f
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

            val groupSpace = 0.5f
            val barSpace = 0.05f
            val barWidth = 0.2f // Sesuaikan lebar bar

            data.barWidth = barWidth
            this.groupBars(0f, groupSpace, barSpace)

            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = labels.size.toFloat()
            xAxis.setCenterAxisLabels(true)

            // Memusatkan bar chart
            setExtraOffsets(10f, 10f, 10f, 10f)
            moveViewToX(xAxis.axisMaximum / 2)

            animateY(1000)
            invalidate()
        }
    }

    private class KgValueFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "${String.format("%.1f", value)} kg"
        }
    }

    private fun getMonthFromDate(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
            val date = sdf.parse(dateString)
            date?.let {
                val calendar = Calendar.getInstance()
                calendar.time = date
                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                monthFormat.format(calendar.time)
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
