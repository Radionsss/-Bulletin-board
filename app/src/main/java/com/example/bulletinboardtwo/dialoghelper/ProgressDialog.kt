package com.example.bulletinboardtwo.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.example.bulletinboardtwo.databinding.ProgressDialogLayoutBinding
import com.example.bulletinboardtwo.databinding.SignDialogActivityBinding

object ProgressDialog {
    fun createProgressDialog(act: Activity): AlertDialog {
        val builder = AlertDialog.Builder(act)
        val binding = ProgressDialogLayoutBinding.inflate(act.layoutInflater)
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.setCancelable(false)

        dialog.show()
        return dialog
    }
}