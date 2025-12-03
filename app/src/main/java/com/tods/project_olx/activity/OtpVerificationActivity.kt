package com.tods.project_olx.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.tods.project_olx.databinding.ActivityOtpVerificationBinding

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerificationBinding
    private lateinit var auth: FirebaseAuth

    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Verify code"

        auth = FirebaseAuth.getInstance()
        verificationId = intent.getStringExtra("verificationId")

        binding.buttonVerify.setOnClickListener(View.OnClickListener {
            val code = binding.editCode.text.toString().trim()
            if (code.isEmpty() || verificationId == null) {
                Toast.makeText(this, "Enter code", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        })
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Phone login successful", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Invalid code", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
