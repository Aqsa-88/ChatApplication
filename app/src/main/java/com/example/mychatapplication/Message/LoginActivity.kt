package com.example.mychatapplication.Message

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mychatapplication.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
class LoginActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog

    private lateinit var mobileEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // aapka login XML

        // Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://mychatapplication-ffdb9-default-rtdb.firebaseio.com/")
            .child("users")

        // UI bindings
        mobileEt = findViewById(R.id.l_mobile)   // EditText for mobile
        emailEt = findViewById(R.id.l_email)     // EditText for email
        loginBtn = findViewById(R.id.l_loginBtn) // Login button
        registerTv = findViewById(R.id.l_r_register)

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Logging in...")



        // 🔹 Register TextView click listener
        registerTv.setOnClickListener {
            val intent = Intent(this@LoginActivity, Register::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            val mobile = mobileEt.text.toString().trim()
            val email = emailEt.text.toString().trim()


            if (mobile.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            MemoryData.Companion.saveUserMobile(this, mobile)
            Log.d("LoginDebug", "User Mobile Saved: ${MemoryData.Companion.getUserMobile(this)}")

            progressDialog.show()

            // Firebase check
            databaseReference.child(mobile).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    progressDialog.dismiss()
                    if (snapshot.exists()) {
                        val dbEmail = snapshot.child("email").getValue(String::class.java)
                        val name = snapshot.child("name").getValue(String::class.java)

                        if (dbEmail == email) {
                            // Login success
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("mobile", mobile)
                            intent.putExtra("name", name)
                            intent.putExtra("email", email)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Email does not match", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "User not registered", Toast.LENGTH_SHORT).show()
                    }
                }


                override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
                    Toast.makeText(this@LoginActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}