package com.example.bulletinboardtwo.dialoghelper

import android.app.AlertDialog
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import com.example.bulletinboardtwo.accounthelper.AccountHelper
import com.example.bulletinboardtwo.MainActivity
import com.example.bulletinboardtwo.R
import com.example.bulletinboardtwo.databinding.SignDialogActivityBinding

class DialogHelper(val act: MainActivity) {
    val accountHelper = AccountHelper(act)
    private var isVisible = false

    fun createSignDialog(index: Int) {
        val builder = AlertDialog.Builder(act)
        val binding = SignDialogActivityBinding.inflate(act.layoutInflater)
        builder.setView(binding.root)

        setDialogState(index, binding)
        val dialog = builder.create()

        binding.btnSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, dialog, binding)
        }
        binding.btnForgetSign.setOnClickListener {
            setOnClickResetPassword(dialog, binding)
        }
        binding.btnSignWithGoogle.setOnClickListener {
            accountHelper.signInWithGoogle()
            dialog.dismiss()
        }

        visibilityPassword(binding)

        dialog.show()
    }

    private fun setDialogState(index: Int, binding: SignDialogActivityBinding) {
        if (index == DialogConst.SIGN_UP_INDEX) {
            binding.signUpDialog.text = act.resources.getString(R.string.registration)
            binding.btnSignUpIn.text = act.resources.getString(R.string.ad_sign_up)
        } else {
            binding.signUpDialog.text = act.resources.getString(R.string.sign_in)
            binding.btnSignUpIn.text = act.resources.getString(R.string.ad_sign_in)
            binding.btnForgetSign.visibility = View.VISIBLE
        }
    }

    private fun setOnClickResetPassword(dialog: AlertDialog?, binding: SignDialogActivityBinding) {
        if (binding.edEmailSign.text.isNotEmpty()) {
            act.myAuth.sendPasswordResetEmail(binding.edEmailSign.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(act, R.string.password_reset_was_send, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            dialog?.dismiss()
        } else {
            binding.tvResetPassword.visibility = View.VISIBLE
        }
    }


    private fun setOnClickSignUpIn(
        index: Int,
        dialog: AlertDialog?,
        binding: SignDialogActivityBinding
    ) {
        dialog?.dismiss()
        if (index == DialogConst.SIGN_UP_INDEX) {
            accountHelper.signUpWithEmail(
                binding.edEmailSign.text.toString(),
                binding.edPasswordSign.text.toString()
            )
        }else {
            accountHelper.signInWithEmail(
                binding.edEmailSign.text.toString(),
                binding.edPasswordSign.text.toString()
            )
        }
    }

    private fun visibilityPassword(binding: SignDialogActivityBinding) {
        binding.visibleUnVisible.setOnClickListener {
            if (!isVisible) {
                binding.edPasswordSign.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                isVisible = true
            } else {
                binding.edPasswordSign.transformationMethod =
                    PasswordTransformationMethod.getInstance();
                isVisible = false
            }
            //Log.d("MyLog", "${isVisible}")
        }
    }
}