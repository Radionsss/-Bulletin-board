package com.example.bulletinboardtwo.accounthelper

import android.util.Log
import android.widget.Toast
import com.example.bulletinboardtwo.const.FireBaseErrorConst
import com.example.bulletinboardtwo.MainActivity
import com.example.bulletinboardtwo.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*


class AccountHelper(val act: MainActivity) {
    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.myAuth.currentUser?.delete()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    act.myAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signInWithEmailIsSuccessful(task.result.user!!)
                            } else {0
                                signInWithEmailException(task.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun signInWithEmailIsSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun signInWithEmailException(e: java.lang.Exception, email: String, password: String) {
        // Log.d("MyLog", "exception:${exception.exception}")
        if (e is FirebaseAuthUserCollisionException) {
            val exception = e as FirebaseAuthUserCollisionException
            //  Log.d("MyLog", "Exception" + exception.errorCode)
            if (exception.errorCode == FireBaseErrorConst.ERROR_EMAIL_ALREADY_IN_USE) {//link email
                linkEmailToGoogle(email, password)
            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {
            val exception = e as FirebaseAuthInvalidCredentialsException
            Log.d("MyLog", "Exception " + exception.errorCode)
            if (exception.errorCode == FireBaseErrorConst.ERROR_INVALID_EMAIL) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.not_correct_email),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {
            val exception = e as FirebaseAuthWeakPasswordException
            Log.d("MyLog", "Exception " + exception.errorCode)
            if (exception.errorCode == FireBaseErrorConst.ERROR_WEAK_PASSWORD) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.error_small_password),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.myAuth.currentUser?.delete()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    act.myAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                act.uiUpdate(task.result?.user)
                            } else {
                                signInWithEmailException(task.exception!!)
                            }
                        }
                }
            }
        }
    }

    private fun signInWithEmailException(e: java.lang.Exception){
        if (e is FirebaseAuthInvalidCredentialsException) {
          //  Log.d("MyLog", "Exception${e}")
            val exception = e as FirebaseAuthInvalidCredentialsException
            //Log.d("MyLog", "Exception" + exception.errorCode)
            if (exception.errorCode == FireBaseErrorConst.ERROR_INVALID_EMAIL) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.not_correct_email),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (exception.errorCode == FireBaseErrorConst.ERROR_WRONG_PASSWORD) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.not_correct_password_or_email),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (e is FirebaseAuthInvalidUserException) {
            val exception = e as FirebaseAuthInvalidUserException
            Log.d("MyLog", "Exception" + exception.errorCode)
            if (exception.errorCode == FireBaseErrorConst.ERROR_USER_NOT_FOUND) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.account_does_not_exist),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun linkEmailToGoogle(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)//предоставление
        if (act.myAuth.currentUser != null) {
            act.myAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        act,
                        act.resources.getString(R.string.email_link_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                act,
                act.resources.getString(R.string.have_account),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.email_send_verification),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.email_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    /*google sign In*//*google sign In*//*google sign In*//*google sign In*//*google sign In*/
    private fun getSignInClientGoogle(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(act, gso)
    }

    fun signInWithGoogle() {
        signInClient = getSignInClientGoogle()
        val intent = signInClient.signInIntent
        act.lunchActGoogle.launch(intent)
    }

    fun signOutGoogle() {
        getSignInClientGoogle().signOut()
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.myAuth.currentUser?.delete()?.addOnCompleteListener {
            if (it.isSuccessful) {
                act.myAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            act,
                            "sign in done",
                            Toast.LENGTH_SHORT
                        ).show()
                        act.uiUpdate(task.result?.user)
                    } else {
                        Log.d("MyLog", "google sign in Exception: ${task.exception}")
                    }
                }
            }
        }
    }
    /*google sign In*//*google sign In*//*google sign In*//*google sign In*//*google sign In*/

    fun signInAnonymously(listener: CompleteListener) {
        act.myAuth.signInAnonymously().addOnCompleteListener {
            if (it.isSuccessful) {
                listener.onComplete()
                Toast.makeText(act, "Вы вошли как гость", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(act, "Не удалось войти как гость ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface CompleteListener {
        fun onComplete()
    }
}