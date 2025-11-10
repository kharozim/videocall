package com.neokarya.sdk_videocall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.neokarya.sdk_videocall.databinding.ActivityWebViewBinding
import kotlin.collections.orEmpty
import kotlin.toString

class WebViewActivity : AppCompatActivity() {
  companion object {
    private const val PERMISSION_REQ_ID = 22
  }

  private val binding by lazy { ActivityWebViewBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(binding.root)
    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    getData()
    setView()
  }


  private fun setView() {
    binding.btnSearch.setOnClickListener {
      val url = binding.edtUrl.text.toString().trim()
      binding.webView.loadUrl(url)
    }
    binding.btnOpenBrowser.setOnClickListener {
      val url = binding.edtUrl.text.toString().trim()
      startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
    binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
  }

  private fun getData() {
    binding.webView.apply {
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
        this,
        Manifest.permission.CAMERA
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      permissionsToRequest.add(Manifest.permission.CAMERA)
    }

    if (needsAudioPermission && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.RECORD_AUDIO
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
    }

    if (permissionsToRequest.isNotEmpty()) {
      // Meminta izin kepada pengguna
      ActivityCompat.requestPermissions(
        this,
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