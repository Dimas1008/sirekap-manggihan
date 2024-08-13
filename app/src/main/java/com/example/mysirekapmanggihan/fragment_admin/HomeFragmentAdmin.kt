package com.example.mysirekapmanggihan.fragment_admin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mysirekapmanggihan.R
import com.example.mysirekapmanggihan.adapter.SliderAdapter
import com.example.mysirekapmanggihan.adapter.SliderAdminAdapter
import com.example.mysirekapmanggihan.databinding.FragmentHomeAdminBinding
import com.example.mysirekapmanggihan.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
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
import com.smarteist.autoimageslider.SliderView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragmentAdmin : Fragment() {

    private lateinit var binding: FragmentHomeAdminBinding
    private lateinit var barChart: BarChart
    private lateinit var firebaseRef: DatabaseReference
    lateinit var imgUrl: ArrayList<String>
    lateinit var sliderAdminAdapter: SliderAdminAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the image URLs for the slider
        imgUrl = ArrayList()

        imgUrl.add("android.resource://" + requireContext().packageName + "/" + R.drawable.img_sampah4)
        imgUrl.add("android.resource://" + requireContext().packageName + "/" + R.drawable.img_sampah3)
        imgUrl.add("android.resource://" + requireContext().packageName + "/" + R.drawable.img_sampah1)

        // Set up the slider adapter
        sliderAdminAdapter = SliderAdminAdapter(imgUrl)
        binding.imageAdminSlider.autoCycleDirection = SliderView.LAYOUT_DIRECTION_RTL
        binding.imageAdminSlider.setSliderAdapter(sliderAdminAdapter)
        binding.imageAdminSlider.scrollTimeInSec = 3
        binding.imageAdminSlider.isAutoCycle = true
        binding.imageAdminSlider.startAutoCycle()

        barChart = binding.barAdminChart


        // Set up BarChart and Firebase reference
        firebaseRef = FirebaseDatabase.getInstance().getReference("sampah")

        fetchChartData()

    }

    // Function to fetch chart data from Firebase
    private fun fetchChartData() {
        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val (beratOrganikPerBulan, beratNonOrganikPerBulan, logamPerBulan) = processData(snapshot)
                displayChart(beratOrganikPerBulan, beratNonOrganikPerBulan, logamPerBulan)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to process data from Firebase snapshot
    private fun processData(snapshot: DataSnapshot): Triple<Map<String, Float>, Map<String, Float>, Map<String, Float>> {
        val beratOrganikPerBulan = mutableMapOf<String, Float>()
        val beratNonOrganikPerBulan = mutableMapOf<String, Float>()
        val logamPerBulan = mutableMapOf<String, Float>()

        // Read data from each sampah data in Firebase
        for (dataSnapshot in snapshot.children) {
            val berat = dataSnapshot.child("berat").getValue(Int::class.java)
            val tanggal = dataSnapshot.child("tanggal").getValue(String::class.java)
            val jenis = dataSnapshot.child("jenis").getValue(String::class.java)

            Log.d("FirebaseData", "Berat: $berat, Tanggal: $tanggal, Jenis: $jenis")

            // Insert data into the map based on jenis and month
            if (berat != null && tanggal != null && jenis != null) {
                val month = getMonthFromDate(tanggal)
                when (jenis) {
                    "Kaca" -> beratOrganikPerBulan[month] = beratOrganikPerBulan.getOrDefault(month, 0f) + berat
                    "Plastik" -> beratNonOrganikPerBulan[month] = beratNonOrganikPerBulan.getOrDefault(month, 0f) + berat
                    "Logam" -> logamPerBulan[month] = logamPerBulan.getOrDefault(month, 0f) + berat
                }
            }
        }

        return Triple(beratOrganikPerBulan, beratNonOrganikPerBulan, logamPerBulan)
    }

    // Function to display the chart with the processed data
    private fun displayChart(
        beratOrganikPerBulan: Map<String, Float>,
        beratNonOrganikPerBulan: Map<String, Float>,
        logamPerBulan: Map<String, Float>,
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

        binding.barAdminChart.apply {
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

// Data class to return 3 values
data class Triple<A, B, C>(
    val first: A,
    val second: B,
    val third: C
)