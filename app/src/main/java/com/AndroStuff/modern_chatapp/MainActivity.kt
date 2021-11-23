package com.AndroStuff.modern_chatapp

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var mobilenumber : EditText
    private lateinit var entereedOtp : EditText
    private lateinit var sendOtpButton : Button
    private lateinit var submitOtpButton: Button
    private lateinit var Profile: ImageView
    private lateinit var uname: EditText
    private lateinit var setup: Button
    private lateinit var auth : FirebaseAuth;
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var storedVerificationId:String = ""
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        mobilenumber = findViewById(R.id.user_mobile_number)
        entereedOtp = findViewById(R.id.user_entered_otp)
        sendOtpButton = findViewById(R.id.get_otp_button)
        submitOtpButton = findViewById(R.id.submit_otp)

        Profile = findViewById(R.id.profile)
        uname = findViewById(R.id.user_name)
        setup = findViewById(R.id.setup)
        sendOtpButton.setOnClickListener {

            if(mobilenumber.text.isNotEmpty()){
                val phoneNumber = mobilenumber.text.toString()

                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }else{
                Toast.makeText(this,"Enter Correct Mobile Number",Toast.LENGTH_LONG).show()
            }
        }

        submitOtpButton.setOnClickListener {

            val code = entereedOtp.text.toString()
            if(code.isNotEmpty()){
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId, code)
                signInWithPhoneAuthCredential(credential)
            }else{
                Toast.makeText(this,"Enter OTP",Toast.LENGTH_SHORT).show()
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {


               Log.d("key","sended")

                storedVerificationId = p0
                resendToken = p1

                SetVisibility(0)
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                SetVisibility(1)
            }

            override fun onVerificationFailed(p0: FirebaseException) {

            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    Toast.makeText(this,"Welcome to Next Generation ChatAPP",Toast.LENGTH_LONG).show()

                    SetVisibility(1)
                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Toast.makeText(this,"failure",Toast.LENGTH_LONG).show()
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    fun SetVisibility(requestCode : Int){

        if(requestCode == 0){
            mobilenumber.visibility = View.GONE
            sendOtpButton.visibility = View.GONE

            entereedOtp.visibility = View.VISIBLE
            submitOtpButton.visibility = View.VISIBLE
        }

        if(requestCode == 1){

            entereedOtp.visibility = View.GONE
            submitOtpButton.visibility = View.GONE


            Profile.visibility = View.VISIBLE
            uname.visibility = View.VISIBLE
            setup.visibility = View.VISIBLE
        }

    }
}