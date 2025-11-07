package com.neokarya.sdk_videocall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.neokarya.sdk_videocall.databinding.ActivityVideocallBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

internal class MainActivity : AppCompatActivity() {

  companion object {
    private const val PERMISSION_REQ_ID = 22
    private const val myUid = 20240004
  }

  private val binding by lazy { ActivityVideocallBinding.inflate(layoutInflater) }
  private var myAppId = ""
  private var channelName = ""
  private var token = ""
  private var mRtcEngine: RtcEngine? = null
  private var lastAction = ""
  private val mRtcEventHandler = object : IRtcEngineEventHandler() {
    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
      super.onJoinChannelSuccess(channel, uid, elapsed)
      runOnUiThread {
        showToast("Joined channel $channel")
        lastAction = "onJoinChannelSuccess"
        setResultData(uid)
        setViewAction(ACTION.ON_CALL_REQUEST)
        Log.d("TAG", "onJoinChannelSuccess: $channel $uid")
      }
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
      runOnUiThread {
        // Aktifkan audio Anda
        mRtcEngine?.muteLocalAudioStream(false)

        // Aktifkan video Anda (jika video digunakan)
        mRtcEngine?.muteLocalVideoStream(false)
        setupLocalVideo()
        setupRemoteVideo(uid)
        showToast("User joined: $uid")
        lastAction = "onUserJoined"
        setResultData(uid)
        setViewAction(ACTION.USER_JOINED)
        Log.d("TAG", "onUserJoined: $uid")
      }

    }

    override fun onUserOffline(uid: Int, reason: Int) {
      super.onUserOffline(uid, reason)
      runOnUiThread {
        mRtcEngine?.stopPreview()
        showToast("User offline: $uid $reason")
        lastAction = "onUserOffline"
        setResultData(uid)
        setViewAction(ACTION.USER_ENDED)
        Log.d("TAG", "onUserOffline: $uid")
      }
    }
  }

  private fun setViewAction(action: ACTION) {
    when (action) {
      ACTION.ON_CALL_REQUEST -> {
        binding.tvCallRequest.isVisible = true
        binding.tvCallRequest.text = "Call request.."
//        binding.btnCallAction.setImageResource(R.drawable.ic_call_end)
        binding.btnCallAction.setOnClickListener {
          mRtcEngine?.disableVideo()
          mRtcEngine?.stopPreview()
          mRtcEngine?.leaveChannel()
          setViewAction(ACTION.USER_ENDED)
        }
      }

      ACTION.USER_JOINED -> {
        binding.tvCallRequest.isVisible = false
//        binding.btnCallAction.setImageResource(R.drawable.ic_call_end)
        binding.btnCallAction.setOnClickListener {
          mRtcEngine?.disableVideo()
          mRtcEngine?.stopPreview()
          mRtcEngine?.leaveChannel()
          setViewAction(ACTION.USER_ENDED)
        }
      }

      ACTION.USER_ENDED -> {
        binding.remoteVideoViewContainer.removeAllViews()
        binding.localVideoViewContainer.removeAllViews()
        binding.tvCallRequest.isVisible = true
        binding.tvCallRequest.text = "Call Ended.."
//        binding.btnCallAction.setImageResource(R.drawable.ic_call_out)
        binding.btnCallAction.setOnClickListener {
          onBackPressedDispatcher.onBackPressed()
        }
      }
    }
  }

  private fun setResultData(uid: Int) {
    val bundle = Bundle()
    bundle.putString(com.neokarya.sdk_videocall.Constants.KEY_ACTION, lastAction)
    bundle.putString(com.neokarya.sdk_videocall.Constants.KEY_USER_ID, uid.toString())
    val intent = Intent().putExtras(bundle)
    setResult(RESULT_OK, intent)
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

    getData()
    if (checkPermissions()) {
      startVideoCalling()
    } else {
      requestPermissions()
    }
  }

  private fun getData() {
    val bundle = intent.extras
    myAppId = bundle?.getString(com.neokarya.sdk_videocall.Constants.KEY_APP_ID, "").orEmpty()
    token = bundle?.getString(com.neokarya.sdk_videocall.Constants.KEY_TOKEN, "").orEmpty()
    channelName = bundle?.getString(com.neokarya.sdk_videocall.Constants.KEY_CHANNEL, "").orEmpty()
  }


  private fun requestPermissions() {
    ActivityCompat.requestPermissions(this, getRequiredPermissions(),
      PERMISSION_REQ_ID
    )
  }

  private fun checkPermissions(): Boolean {
    for (permission in getRequiredPermissions()) {
      if (ContextCompat.checkSelfPermission(
          this,
          permission
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        return false
      }
    }
    return true
  }

  private fun getRequiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.BLUETOOTH_CONNECT
      )
    } else {
      arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
      )
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PERMISSION_REQ_ID && checkPermissions()) {
      startVideoCalling()
    }
  }


  private fun startVideoCalling() {
    initializeRtcEngine()
    setVideoScenario()
    enableVideo()
//    setupLocalVideo()
    joinChannel()
  }

  private fun setVideoScenario() {
    mRtcEngine?.apply {
      setVideoScenario(io.agora.rtc2.Constants.VideoScenario.APPLICATION_SCENARIO_1V1)
    }
  }

  private fun initializeRtcEngine() {
    if (mRtcEngine == null) {
      try {
        val config = RtcEngineConfig().apply {
          mContext = applicationContext
          mAppId = myAppId
          mEventHandler = mRtcEventHandler
        }
        mRtcEngine = RtcEngine.create(config)
      } catch (e: Exception) {
        throw RuntimeException("Error initializing RTC engine: ${e.message}")
      }
    }
  }


  private fun enableVideo() {
    mRtcEngine?.apply {
      enableVideo()
      startPreview()
    }
  }


  /**
   * Initializes the local video view and sets the display properties.
   * This method adds a SurfaceView to the local video container and configures it.
   */
  private fun setupLocalVideo() {
    val container: FrameLayout = binding.localVideoViewContainer
    val surfaceView = SurfaceView(baseContext)
    container.addView(surfaceView)
    mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT,
      myUid
    ))
  }

  private fun joinChannel() {
    val options = ChannelMediaOptions().apply {
      clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER
      channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
      publishMicrophoneTrack = true
      publishCameraTrack = true
    }
    mRtcEngine?.joinChannel(token, channelName,
      myUid, options)
  }

  private fun setupRemoteVideo(uid: Int) {
    val container = binding.remoteVideoViewContainer
    val surfaceView = SurfaceView(baseContext).apply {
      setZOrderMediaOverlay(true)
    }
    container.addView(surfaceView)
    mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
  }


  override fun onDestroy() {
    super.onDestroy()
    cleanupAgoraEngine()
  }

  private fun cleanupAgoraEngine() {
    mRtcEngine?.apply {
      stopPreview()
      leaveChannel()
    }
    mRtcEngine = null
  }

  private fun showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }
}