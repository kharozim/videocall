package com.neokarya.sdk_videocall.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * Created by Kharozim
 * 06/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */
class RepositoryImpl(private val api: Api) : Repository {
  override suspend fun getData(): Result<List<PicsumModel>> {
    try {
      val response = withContext(Dispatchers.IO) { api.getData() }
      if (response.isSuccessful) {
        val data = response.body().orEmpty()
        return Result.success(data.asSequence().map { it.toModel() }.toList())
      }
      return Result.failure(Throwable("Failed to get data. Code:${response.code()}"))
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }
}