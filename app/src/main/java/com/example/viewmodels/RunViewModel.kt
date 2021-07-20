package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Repository.MainRepository
import com.example.localDb.Run
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RunViewModel
@Inject constructor(private val mainRepository: MainRepository) : ViewModel() {


    val runList = mainRepository.getAllRun()

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

}