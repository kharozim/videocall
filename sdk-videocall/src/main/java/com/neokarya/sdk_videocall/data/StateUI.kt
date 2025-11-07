package com.neokarya.sdk_videocall.data

sealed class StateUI<out T> {
  data class Success<T>(val data: T) : StateUI<T>()
  object Loading : StateUI<Nothing>()
  data class Error(val message: String) : StateUI<Nothing>()
}