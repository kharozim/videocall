package com.neokarya.sdk_videocall

import android.content.Context
import android.content.Intent
import android.os.Bundle

/*
 * Created by Kharozim
 * 04/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */
open class VideoCallUrl {
  companion object {
    fun with(context: Context): Builder = VideoCallUrl().Builder(context)
  }

  inner class Builder(private val context: Context) {
    private var urlVideoCall: String = ""

    fun init(urlVideoCall: String): Builder {
      this.urlVideoCall = urlVideoCall
      return this
    }

    fun startWithIntent(intent: (Intent) -> Unit) {
      val bundle = Bundle().apply {
        putString(Constants.KEY_VIDEO_CALL_URL, urlVideoCall)
      }
      intent.invoke(
        Intent(context, WebViewActivity::class.java).putExtras(bundle)
      )
    }

    fun start() {
      val bundle = Bundle().apply {
        putString(Constants.KEY_VIDEO_CALL_URL, urlVideoCall)
      }
      val intent = Intent(context, WebViewActivity::class.java).putExtras(bundle)
      context.startActivity(intent)
    }

  }
}