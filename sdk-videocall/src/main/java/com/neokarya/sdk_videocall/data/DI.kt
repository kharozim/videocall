package com.neokarya.sdk_videocall.data

import android.content.Context

/*
 * Created by Kharozim
 * 06/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */
object DI {

  fun provideRepository(context: Context): Repository {
    val okhttp = NetworkUtil.provideOkhttp(context)
    val retrofit = NetworkUtil.provideRetrofit(okhttp)
    val api = NetworkUtil.provideApi(retrofit)
    return RepositoryImpl(api)
  }
}