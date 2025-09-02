package com.example.mychatapplication.Message

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mychatapplication.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Register : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog

    private lateinit var nameEt: EditText
    private lateinit var mobileEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var registerBtn: Button
    @SuppressLint("ResourceType")
    private fun showPopup(message: String, imageRes: Int) {
        val dialogView = layoutInflater.inflate(R.layout.popup_dialog, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.popupImage)
        val textView = dialogView.findViewById<TextView>(R.id.popupMessage)

        imageView.setImageResource(imageRes)
        textView.text = message


        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.id.popupMessage)
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing && !isFinishing) {
                dialog.dismiss()
        }}, 2000)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")


        // UI bindings
        nameEt = findViewById(R.id.r_name)
        mobileEt = findViewById(R.id.r_mobile)
        emailEt = findViewById(R.id.r_email)
        registerBtn = findViewById(R.id.r_registerBtn)

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Registering...")

        // Register button
        registerBtn.setOnClickListener {
            val name = nameEt.text.toString().trim()
            val mobile = mobileEt.text.toString().trim()
            val email = emailEt.text.toString().trim()

            if (name.isEmpty() || mobile.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPopup("Registered Successfully", R.raw.success)

            progressDialog.show()

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(mobile)) {
                        progressDialog.dismiss()
                        Toast.makeText(this@Register, "Mobile Already Exists", Toast.LENGTH_SHORT).show()
                    } else {
                        // 🔹 Initialize imageName as empty string
                        val imageName = "" // database me name ka placeholder

                        // Save user data
                        val userMap = mapOf(
                            "name" to name,
                            "email" to email,
                            "imageName" to imageName
                        )

                        databaseReference.child(mobile).setValue(userMap)
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                Toast.makeText(this@Register, "Registration Success ✅", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@Register, MainActivity::class.java)
                                intent.putExtra("mobile", mobile)
                                intent.putExtra("name", name)
                                intent.putExtra("email", email)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                progressDialog.dismiss()
                                Toast.makeText(this@Register, "Registration Failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
                    Toast.makeText(this@Register, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}



//import android.app.ProgressDialog
//import android.content.Intent
//import android.os.Bundle
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.AppCompatButton
//import androidx.activity.enableEdgeToEdge
//import com.google.firebase.database.*
//
//class Register : AppCompatActivity() {
//
//    // Firebase ka reference (Database ka root node "users")
//    private lateinit var databaseReference: DatabaseReference
//
//    // Progress dialog (loading dikhane ke liye)
//    private lateinit var progressDialog: ProgressDialog
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_register)
//
//        // ProgressDialog init (jab data save ya check ho raha ho to loading show hoga)
//        progressDialog = ProgressDialog(this)
//        progressDialog.setCancelable(false)
//        progressDialog.setMessage("Loading...")
//
//
//        // check if user already logged in
//        if(!MemoryData.getData(this).isEmpty()){
//            val intent = Intent(this@Register, MainActivity::class.java)
//            intent.putExtra("mobile", MemoryData.getData(this));
//            intent.putExtra("name", MemoryData.getData(this));
//            intent.putExtra("email", "")
//            startActivity(intent)
//            finish() // is screen ko close kar denaIntent intent = new Intent(@Register,MainActivity.class);
//
//        }
//
//        // Firebase reference initialize (users node ke andar ka data manage hoga)
//        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://chat-application-d6214-default-rtdb.firebaseio.com/")
//
//        // Views ko findViewById se connect karna
//        val name = findViewById<EditText>(R.id.r_name)
//        val mobile = findViewById<EditText>(R.id.r_mobile)
//        val email = findViewById<EditText>(R.id.r_email)
//        val registerBtn = findViewById<AppCompatButton>(R.id.r_registerBtn)
//
//
//        // Register button click event
//        registerBtn.setOnClickListener {
//            val nameTxt = name.text.toString().trim()
//            val mobileTxt = mobile.text.toString().trim()
//            val emailTxt = email.text.toString().trim()
//
//            // --- Validation section ---
//            if (nameTxt.isEmpty()) {
//                name.error = "Name required"
//                return@setOnClickListener
//            }
//            if (mobileTxt.isEmpty()) {
//                mobile.error = "Mobile required"
//                return@setOnClickListener
//            }
//            if (emailTxt.isEmpty()) {
//                email.error = "Email required"
//                return@setOnClickListener
//            }
//
//            // Loading dikhana
//            progressDialog.show()
//
//            // Firebase me check karna ke ye mobile number already exist to nahi karta
//            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    progressDialog.dismiss()
//
//                    // Agar user ka mobile number already exist karta hai to error
//                    if (snapshot.hasChild(mobileTxt)) {
//                        Toast.makeText(
//                            this@Register,
//                            "Mobile Already Exists",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    } else {
//                        // Agar mobile number exist nahi karta, to naya user save karo
//                        databaseReference.child(mobileTxt).child("name").setValue(nameTxt)
//                        databaseReference.child(mobileTxt).child("email").setValue(emailTxt)
//
//                        // Mobile ko local memory me save karna (SharedPreferences jaisa)
//                        MemoryData.saveData(mobileTxt, this@Register)
//                        // save name to memory
//                        MemoryData.saveName(nameTxt,this@Register)
//
//                        // Success message
//                        Toast.makeText(this@Register, "Registration Success", Toast.LENGTH_SHORT).show()
//
//                        // Register ke baad MainActivity par move karna aur user data bhejna
//                        val intent = Intent(this@Register, MainActivity::class.java)
//                        intent.putExtra("mobile", mobileTxt)
//                        intent.putExtra("name", nameTxt)
//                        intent.putExtra("email", emailTxt)
//                        startActivity(intent)
//                        finish() // is screen ko close kar dena
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    // Agar database me error aaye to user ko show karo
//                    progressDialog.dismiss()
//                    Toast.makeText(
//                        this@Register,
//                        "Database Error: ${error.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            })
//        }
//    }
//}
