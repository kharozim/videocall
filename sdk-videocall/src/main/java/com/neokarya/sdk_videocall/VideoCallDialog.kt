package com.neokarya.sdk_videocall

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var binding: DialogWebviewBinding

    private val factory by lazy {
      val repo = DI.provideRepository(requireContext())
      VideoCallViewModelFactory(repo)
    }
    private val viewModel: VideoCallViewModel by viewModels { factory }
    private var urlVideoCall: String = ""

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
              }
            }
          }
        }

      }
      binding.btnCallEnd.setOnClickListener {
        dismiss()
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
  }

}