package com.example.viewmodels

import androidx.lifecycle.ViewModel
import com.example.Repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel
 @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {

}