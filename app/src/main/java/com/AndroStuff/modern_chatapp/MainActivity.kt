package com.AndroStuff.modern_chatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    //Declaration
    private lateinit var storage:FirebaseStorage
    private lateinit var profileUri:Uri
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
    private lateinit var imagefile:Uri
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private val PICK_CODE = 1000;
    var permissionGranted : Boolean = false
    private lateinit var   storageReference:StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        storage=Firebase.storage
        storageReference=storage.reference
        //this below will bypass repetation of otp if auth?
    /*if(auth.currentUser!=null)
        SetVisibility(1)*/



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

        setup.setOnClickListener {
            SaveData()
            uploadtocloudstorage(profileUri)
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
        val intent=Intent()
        intent.type="image/*"
        intent.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select Profile"),PICK_CODE)


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

    fun uploadtocloudstorage(file_uri:Uri){
        val progressDialog:ProgressDialog
//        progressDialog=ProgressDialog(applicationContext)
//        progressDialog.setTitle("plz wait...")
//        progressDialog.show()

        val phonenumbername = auth.currentUser?.phoneNumber.toString()
        storageReference.child(phonenumbername+"_" + uname.text).putFile(file_uri).addOnSuccessListener {
            Toast.makeText(this, "Profile Stored On Server Successfully",
                Toast.LENGTH_SHORT).show()
    //            progressDialog.setTitle("sorry")
    //            progressDialog.dismiss();
        }
            .addOnCanceledListener {
            Toast.makeText(this, "Not yet Uploaded",
            Toast.LENGTH_SHORT).show()
    //            progressDialog.dismiss()
        }

    }

    fun SaveData(){

        val external = Environment.getExternalStorageState()
        if(external.equals(Environment.MEDIA_MOUNTED)){
            val imageName :String = uname.text.toString() + ".jpg";
            val sd = Environment.getExternalStorageDirectory().toString()
            val file = File(sd,imageName)
            try {
                val stream:OutputStream = FileOutputStream(file)
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(profileUri.toString()))
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
                stream.flush()
                stream.close()


            }catch (e:Exception){

            }

        }else{
            Toast.makeText(this, "Cannot access internal storage", Toast.LENGTH_SHORT).show()

        }
    }



    //override Methods
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
            profileUri = data?.data!!


        }
    }


}