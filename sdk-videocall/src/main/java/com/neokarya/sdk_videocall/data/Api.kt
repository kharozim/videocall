package com.neokarya.sdk_videocall.data

import retrofit2.Response
import retrofit2.http.GET

/*
 * Created by Kharozim
 * 06/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */
interface Api {
  @GET("/v2/list?page=1&limit=2")
  suspend fun getData(): Response<List<PicsumResponse>>
}