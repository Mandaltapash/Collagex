package com.tods.project_olx.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityRegisterBinding
import com.tods.project_olx.model.User

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var selectedCollege: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        auth = Firebase.auth
        configViewBinding()
        configButtonRegisterClickListener()
        configCollegeSpinner()
    }

    private fun configButtonRegisterClickListener() {
        binding.buttonRegister.setOnClickListener(View.OnClickListener {
            validate()
        })

        binding.textSigninNow.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish RegisterActivity so user doesn't come back to it
        })
    }

    private fun configViewBinding() {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun configCollegeSpinner() {
        val colleges = resources.getStringArray(R.array.assam_colleges)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, colleges)
        binding.spinnerCollege.setAdapter(adapter)
        binding.spinnerCollege.threshold = 1 // Show suggestions after 1 character
    }

    private fun register(user: User){
        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    user.save()
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    try {
                        it.exception
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Toast.makeText(this, "Please use a stronger password!", Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException){
                        Toast.makeText(this, "Please use a valid email!", Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "E-mail already in use!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("REGISTER", "INVALID REGISTRATION: ${e.message}")
                    }
                }
            }
    }

    private fun validate(){
        val name = binding.editRegisterName.text.toString()
        val email = binding.editRegisterEmail.text.toString()
        val password = binding.editRegisterPassword.text.toString()
        val confirmPassword = binding.editRegisterConfirmPassword.text.toString()
        selectedCollege = binding.spinnerCollege.text.toString() // Get text from AutoCompleteTextView


        if (name.isNotEmpty()){
            if (email.isNotEmpty()){
                if (password.isNotEmpty()){
                    if (confirmPassword.isNotEmpty()){
                        if (password == confirmPassword){
                            if (selectedCollege.isNotEmpty() && selectedCollege != "Select your college"){ // Check for college selection
                                val user = User()
                                user.name = name
                                user.email = email
                                user.password = password
                                user.collegeName = selectedCollege
                                register(user)
                            } else {
                                Toast.makeText(this, "Please select your college!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Passwords doesn't match!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Field 'Confirm password' cannot be empty!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Field 'Password' cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Field 'E-mail' cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Field 'Name' cannot be empty!", Toast.LENGTH_SHORT).show()
        }
    }
}