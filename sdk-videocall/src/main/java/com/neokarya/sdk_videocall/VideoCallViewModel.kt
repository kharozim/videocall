package com.neokarya.sdk_videocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neokarya.sdk_videocall.data.PicsumModel
import com.neokarya.sdk_videocall.data.Repository
import com.neokarya.sdk_videocall.data.StateUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/*
 * Created by Kharozim
 * 06/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */

internal class VideoCallViewModelFactory(private val repo: Repository) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(VideoCallViewModel::class.java)) {
      return VideoCallViewModel(repo) as T
    }
    throw kotlin.IllegalArgumentException("Unknown model class")
  }
}

internal class VideoCallViewModel(private val repo: Repository) : ViewModel() {

  fun getData(): Flow<StateUI<List<PicsumModel>>> = flow {
    emit(StateUI.Loading)
    try {

      repo.getData().fold(onSuccess = {
        emit(StateUI.Success(it))
      }, onFailure = {
        emit(StateUI.Error(it.message.orEmpty()))
      })

    } catch (e: Exception) {
      emit(StateUI.Error(e.localizedMessage.orEmpty()))
    }
  }

}

