package com.neokarya.sdk_videocall.data

/*
 * Created by Kharozim
 * 06/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */
fun PicsumResponse.toModel(): PicsumModel = PicsumModel(
  author = author.orEmpty(),
  downloadUrl = downloadUrl.orEmpty(),
  height = height ?: 0,
  id = id.orEmpty(),
  url = url.orEmpty(),
  width = width ?: 0
)