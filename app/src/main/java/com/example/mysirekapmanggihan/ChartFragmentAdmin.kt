package com.example.mysirekapmanggihan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mysirekapmanggihan.databinding.FragmentChartAdminBinding
import com.example.mysirekapmanggihan.databinding.FragmentProfileBinding
import com.example.mysirekapmanggihan.preference.Preferences
import com.google.firebase.database.DatabaseReference

class ChartFragmentAdmin : Fragment() {

    private var _binding: FragmentChartAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: Preferences
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart_admin, container, false)
    }
}