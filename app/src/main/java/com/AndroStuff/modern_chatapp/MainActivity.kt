package com.AndroStuff.modern_chatapp

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    //Declaration
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
    private val PICK_CODE = 1000;
    var permissionGranted : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this, Array(2){android.Manifest.permission.READ_EXTERNAL_STORAGE
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},111)
        }else{
            permissionGranted = true
        }


        //Initialization
        auth = FirebaseAuth.getInstance()

        mobilenumber = findViewById(R.id.user_mobile_number)
        entereedOtp = findViewById(R.id.user_entered_otp)
        sendOtpButton = findViewById(R.id.get_otp_button)
        submitOtpButton = findViewById(R.id.submit_otp)

        Profile = findViewById(R.id.profile)
        uname = findViewById(R.id.user_name)
        setup = findViewById(R.id.setup)


        //Click Listeners
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

        Profile.setOnClickListener {
            fetchImage()
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


    //Methods
    private fun fetchImage() {
        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,PICK_CODE)


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




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            permissionGranted = true
        }else{
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            permissionGranted = false

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == PICK_CODE){
            Profile.setImageURI(data?.data)
            val external = Environment.getExternalStorageState()
         /*   if(external.equals(Environment.MEDIA_MOUNTED)){

                val sd = Environment.getExternalStorageDirectory().toString()
                val file = File(sd,"image")
                try {
                    val ssteam:OutputStream = FileOutputStream(file)
                    va
                }catch ()

            }else{
                Toast.makeText(this, "Cannot access internal storage", Toast.LENGTH_SHORT).show()

            }*/
        }
    }
}