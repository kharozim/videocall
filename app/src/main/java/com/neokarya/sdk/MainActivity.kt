package com.neokarya.sdk

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.neokarya.sdk.databinding.ActivityMainBinding
import com.neokarya.sdk_videocall.Constants
import com.neokarya.sdk_videocall.VideoCall
import com.neokarya.sdk_videocall.VideoCallDialog
import com.neokarya.sdk_videocall.VideoCallUrl

class MainActivity : AppCompatActivity() {

  private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
  private val activityResult =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) {
        val action = result.data?.extras?.getString(Constants.KEY_ACTION)
        Toast.makeText(this, action, Toast.LENGTH_SHORT).show()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(binding.root)
    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    setView()

  }

  private fun setView() {
    binding.btnSdk.setOnClickListener {
      VideoCall
        .with(this@MainActivity)
        .init(
          appId = "ed5ad650bd97444a908a53b7a06e6a2c",
          token = "007eJxTYLiw6Vv7/lYm55kv4pZ0PFL+IzrR4chKDo6cebOki43KblYqMKSmmCammJkaJKVYmpuYmCRaGlgkmhonmScamKWaJRolN/7nymwIZGQ4yNnMwsgAgSA+B0NyRmJeXmqOIQMDABrWIU4=",
          channel = "channel1"
        )
        .startWithIntent { intent ->
          activityResult.launch(intent)
        }
    }
    binding.btnUrl.setOnClickListener {
      VideoCallUrl
        .with(this@MainActivity)
        .init(
          urlVideoCall = "https://dashboard.videoverifikasi.com/room/lobby-waiting/RM-cuQvtcr48ZRYx9"
        )
        .startWithIntent { intent ->
          activityResult.launch(intent)
        }
    }

    binding.btnDialog.setOnClickListener {
      VideoCallDialog.with(supportFragmentManager)
        .start()
    }
  }

}