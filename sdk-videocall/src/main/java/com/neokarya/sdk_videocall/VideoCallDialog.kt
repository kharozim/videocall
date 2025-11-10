package com.neokarya.sdk_videocall

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.neokarya.sdk_videocall.data.DI
import com.neokarya.sdk_videocall.data.StateUI
import com.neokarya.sdk_videocall.databinding.DialogWebviewBinding
import kotlinx.coroutines.launch

/*
 * Created by Kharozim
 * 04/11/25 - kharozim.wrk@gmail.com
 * Copyright (c) 2025. VideoCall
 * All Rights Reserved
 */
class VideoCallDialog {

  companion object {
    @JvmStatic
    fun with(fragmentManager: FragmentManager): Builder = Builder(fragmentManager)
  }

  class Builder(private val fragmentManager: FragmentManager) : BottomSheetDialogFragment() {
    companion object {
      private const val PERMISSION_REQ_ID = 22
    }

    private lateinit var binding: DialogWebviewBinding

    private val factory by lazy {
      val repo = DI.provideRepository(requireContext())
      VideoCallViewModelFactory(repo)
    }
    private val viewModel: VideoCallViewModel by viewModels { factory }
    private var urlVideoCall: String = ""

    override fun getTheme(): Int = R.style.Theme_Material3_Light_BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
      val dialog = super.onCreateDialog(savedInstanceState)
      dialog.setOnShowListener { dialogInterface ->
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet =
          bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet)

        bottomSheet?.let {
          val behavior = BottomSheetBehavior.from(it)

          // 1. Atur ketinggian View agar MATCH_PARENT
          it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

          // 2. Paksa state menjadi EXPANDED
          // Ini membuat dialog langsung mengambil ketinggian penuh
          behavior.state = BottomSheetBehavior.STATE_EXPANDED

          // 3. Opsional: Nonaktifkan Draggable/Swipe untuk pengalaman Fullscreen murni
          // behavior.isDraggable = false
        }
      }
      return dialog
    }

    override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
    ): View? {
      binding = DialogWebviewBinding.inflate(inflater, container, false)


      setupWebView()
      setView()

      return binding.root
    }

    private fun setView() {
      binding.root.isVisible = true
      binding.btnCallRequest.setOnClickListener {
        lifecycleScope.launch {
          viewModel.getData().collect { state ->
            when (state) {
              is StateUI.Error -> {
                binding.btnCallRequest.text = state.message
              }

              StateUI.Loading -> {
                binding.btnCallRequest.text = "Loading"
              }

              is StateUI.Success -> {
                binding.btnCallRequest.text = "Sukses ${state.data.size}"
                binding.lRoom.root.isVisible = true
                binding.lForm.isVisible = false
                binding.lRoom.webView.loadUrl(binding.edtName.text.toString().trim())
              }
            }
          }
        }
      }

      binding.btnClose.setOnClickListener {
        dismiss()
      }

      binding.lRoom.btnCallEnd.setOnClickListener {
        binding.lRoom.webView.loadUrl("")
        binding.lRoom.root.isVisible = false
        binding.lForm.isVisible = true
      }
    }

    fun start() {
      if (!isVisible) {
        show(fragmentManager, VideoCallDialog::class.simpleName)
      }
    }

    fun init(urlVideoCall: String): Builder {
      this.urlVideoCall = urlVideoCall
      return this
    }


    private fun setupWebView() {
      binding.lRoom.webView.apply {
        val client = CustomWebViewClient()
        settings.javaScriptEnabled = true
        webViewClient = client
        webChromeClient = object : WebChromeClient() {
          override fun onPermissionRequest(request: PermissionRequest?) {
            Log.d("TAG", "onPermissionRequest: $request")
            checkAndRequestPermissions(request)
          }

        }
      }
    }


    // Menyimpan objek PermissionRequest sementara
    private var pendingPermissionRequest: PermissionRequest? = null

    private fun checkAndRequestPermissions(request: PermissionRequest?) {
      pendingPermissionRequest = request

      // Mengidentifikasi sumber daya yang diminta (kamera, mikrofon)
      val requestedResources = request?.resources

      // Cek apakah izin kamera atau mikrofon diminta
      var needsCameraPermission = false
      var needsAudioPermission = false
      for (resource in requestedResources.orEmpty()) {
        Log.d("TAG", "checkAndRequestPermissions: $resource")
        if (resource == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
          needsCameraPermission = true
        } else if (resource == PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
          needsAudioPermission = true
        }
      }

      val permissionsToRequest = mutableListOf<String>()

      if (needsCameraPermission && ContextCompat.checkSelfPermission(
          requireContext(),
          Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        permissionsToRequest.add(Manifest.permission.CAMERA)
      }

      if (needsAudioPermission && ContextCompat.checkSelfPermission(
          requireContext(),
          Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
      }

      if (permissionsToRequest.isNotEmpty()) {
        // Meminta izin kepada pengguna
        ActivityCompat.requestPermissions(
          requireActivity(),
          permissionsToRequest.toTypedArray(),
          PERMISSION_REQ_ID
        )
      } else {
        // Izin sudah diberikan sebelumnya, langsung izinkan permintaan WebView
        request?.grant(requestedResources)
        pendingPermissionRequest = null
      }
    }

    // --- Hasil Permintaan Izin Android ---

    override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray,
    ) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)

      if (requestCode == PERMISSION_REQ_ID && pendingPermissionRequest != null) {

        val grantedResources = mutableListOf<String>()

        // Cek hasil untuk setiap izin
        for (i in permissions.indices) {
          Log.d("TAG", "onRequestPermissionsResult: ${grantResults[i]} ${permissions[i]}")
          if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
            if (permissions[i] == Manifest.permission.CAMERA) {
              grantedResources.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
            } else if (permissions[i] == Manifest.permission.RECORD_AUDIO) {
              grantedResources.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
            }
          }
        }

        if (grantedResources.isNotEmpty()) {
          // Beri izin di WebView untuk sumber daya yang disetujui
          pendingPermissionRequest?.grant(grantedResources.toTypedArray())
        } else {
          // Tolak izin jika tidak ada yang disetujui
          pendingPermissionRequest?.deny()
        }

        pendingPermissionRequest = null
      }
    }


    // Class WebViewClient Kustom
    private inner class CustomWebViewClient : WebViewClient() {

      // Metode untuk API Level 24 (Nougat) ke atas
      override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val newUrl = request?.url.toString()
//      handleUrlChange(newUrl)
        Log.d("TAG", "shouldOverrideUrlLoading: $newUrl")
        // Membiarkan WebView menangani pemuatan URL
        return super.shouldOverrideUrlLoading(view, request)
      }


      // Metode opsional: Dipanggil saat pemuatan halaman dimulai, termasuk setelah redirect
      override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
        if (url != null) {
          // Berguna untuk melacak URL yang telah dimuat
//        handleUrlChange(url)
          Log.d("TAG", "onPageStarted: $url")
        }
        super.onPageStarted(view, url, favicon)
      }
    }
  }

}