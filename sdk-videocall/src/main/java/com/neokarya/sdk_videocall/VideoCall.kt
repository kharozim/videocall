package com.neokarya.sdk_videocall

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlin.apply
import kotlin.jvm.java

/*
 * Created by Kharozim
 * 04/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */
open class VideoCall {
  companion object {
    fun with(context: Context): Builder = VideoCall().Builder(context)
  }

  inner class Builder(private val context: Context) {
    private var appId: String = ""
    private var channel: String = ""
    private var token: String = ""

    fun init(appId: String, token: String, channel: String): Builder {
      this.appId = appId
      this.token = token
      this.channel = channel
      return this
    }


    fun startWithIntent(intent: (Intent) -> Unit) {
      val bundle = Bundle().apply {
        putString(Constants.KEY_APP_ID, appId)
        putString(Constants.KEY_TOKEN, token)
        putString(Constants.KEY_CHANNEL, channel)
      }
      intent.invoke(
        Intent(context, MainActivity::class.java).putExtras(bundle)
      )
    }

    fun start() {
      val bundle = Bundle().apply {
        putString(Constants.KEY_APP_ID, appId)
        putString(Constants.KEY_TOKEN, token)
        putString(Constants.KEY_CHANNEL, channel)
      }
      context.startActivity(
        Intent(context, MainActivity::class.java)
          .putExtras(bundle)
      )
    }

  }
}