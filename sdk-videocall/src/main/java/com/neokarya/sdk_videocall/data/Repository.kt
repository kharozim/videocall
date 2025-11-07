package com.neokarya.sdk_videocall.data

/*
 * Created by Kharozim
 * 06/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */
interface Repository {
  suspend fun getData(): Result<List<PicsumModel>>
}