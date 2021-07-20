package com.example.ui.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.stepcounterapp.R
import com.example.viewmodels.RunViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment(R.layout.fragment_settings) {
    private val viewModel: RunViewModel by viewModels()
}