package com.neokarya.sdk_videocall.data

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/*
 * Created by Kharozim
 * 06/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */
internal object NetworkUtil {

  fun provideOkhttp(context: Context): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
      setLevel(HttpLoggingInterceptor.Level.BODY)
    }
    return OkHttpClient.Builder()
      .addInterceptor(loggingInterceptor)
      .connectTimeout(10, TimeUnit.SECONDS)
      .callTimeout(10, TimeUnit.SECONDS)
      .readTimeout(10, TimeUnit.SECONDS)
      .build()
  }

  fun provideRetrofit(okhttp: OkHttpClient): Retrofit {
    val gson = GsonBuilder()
      .setStrictness(Strictness.LENIENT)
      .create()
    val convertFactory = GsonConverterFactory.create(gson)
    return Retrofit.Builder()
      .baseUrl("https://picsum.photos/")
      .client(okhttp)
      .addConverterFactory(convertFactory)
      .build()
  }

  fun provideApi(retrofit: Retrofit): Api = retrofit.create(Api::class.java)

}