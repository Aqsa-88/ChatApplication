package com.example.mychatapplication.Message

import com.example.mychatapplication.R
import android.app.AlertDialog
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mychatapplication.Message.messages.MessagesAdapter
import com.example.mychatapplication.Message.messages.MessagesList
import com.google.firebase.database.DatabaseReference
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {

    private val messageLists = ArrayList<MessagesList>()
    private var mobile: String? = null
    private var email: String? = null
    private var name: String? = null

    private lateinit var messageRecycleView: RecyclerView
    private lateinit var userProfilePicture: CircleImageView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var messagesAdapter: MessagesAdapter

    private val PICK_IMAGE_REQUEST = 2001

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

        dialog.window?.setBackgroundDrawableResource(R.color.transparent)
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 1500)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val prefs = getSharedPreferences("MemoryData", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val logoutBtn = findViewById<ImageView>(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { dialog, _ ->

                    val prefs = getSharedPreferences("MemoryData", MODE_PRIVATE)
                    prefs.edit().clear().apply()
                    MemoryData.clearUserMobile(this)

                    showPopup("Logged out successfully", R.drawable.logout)

                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }, 4000)

                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        messageRecycleView = findViewById(R.id.messageRecycleView)
        userProfilePicture = findViewById(R.id.userProfilePicture)

        databaseReference = FirebaseDatabase
            .getInstance()
            .getReferenceFromUrl("https://mychatapplication-ffdb9-default-rtdb.firebaseio.com/")

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading...")
        progressDialog.show()

        mobile = intent.getStringExtra("mobile")
        email = intent.getStringExtra("email")
        name = intent.getStringExtra("name")

        messageRecycleView.layoutManager = LinearLayoutManager(this)
        messagesAdapter = MessagesAdapter(messageLists, this)
        messageRecycleView.adapter = messagesAdapter

        println("Mobile: $mobile, Email: $email, Name: $name")

        // 🔹 Load current user profile picture
        mobile?.let { mob ->
            databaseReference.child("users").child(mob)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val profilePictureUrl =
                            snapshot.child("profile_picture").getValue(String::class.java)
                        if (!profilePictureUrl.isNullOrEmpty()) {
                            Picasso.get().load(profilePictureUrl)
                                .placeholder(R.drawable.chatapp)
                                .into(userProfilePicture)
                        } else {
                            userProfilePicture.setImageResource(R.drawable.chatapp) // default
                        }
                        progressDialog.dismiss()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        progressDialog.dismiss()
                    }
                })
        } ?: run { progressDialog.dismiss() }

        // 🔹 Click on profile picture to upload new one
        userProfilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // 🔹 Fetch all users for chat list
        databaseReference.child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageLists.clear()
                    for (dataSnapshot in snapshot.children) {
                        val getMobile = dataSnapshot.key
                        if (getMobile != null && getMobile != mobile) {
                            val getName =
                                dataSnapshot.child("name").getValue(String::class.java)
                            val getProfilePicture =
                                dataSnapshot.child("profile_picture").getValue(String::class.java)
                            var lastMessage = ""
                            var unreadCount = 0
                            val chatKey =
                                if (mobile!! < getMobile) "$mobile-$getMobile" else "$getMobile-$mobile"
                            if (unreadCount < 0) unreadCount = 0

                            databaseReference.child("chat").child(chatKey).child("messages")
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        var lastMessage = ""
                                        var unreadCount = 0

                                        for (msgData in snapshot.children) {
                                            val msg = msgData.child("msg").getValue(String::class.java) ?: ""
                                            val sender = msgData.child("sender").getValue(String::class.java) ?: ""
                                            val seen = msgData.child("seen").getValue(Boolean::class.java) ?: false

                                            if (msg.isNotEmpty()) {
                                                lastMessage = msg
                                            }
                                            if (!seen && sender != mobile) {
                                                unreadCount++
                                            }
                                        }

                                        val messageList = MessagesList(
                                            name = getName ?: "Unknown",
                                            mobile = getMobile,
                                            lastMessage = lastMessage,
                                            profilePicture = getProfilePicture ?: "",
                                            unseenMessage = unreadCount,
                                            chatKey = chatKey
                                        )

                                        val index = messageLists.indexOfFirst { it.mobile == getMobile }
                                        if (index != -1) {
                                            messageLists[index] = messageList
                                        } else {
                                            messageLists.add(messageList)
                                        }

                                        messagesAdapter.updateData(ArrayList(messageLists))
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // 🔹 Handle image picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null && mobile != null) {
                val storageRef = FirebaseStorage.getInstance()
                    .reference.child("profile_images/${mobile}.jpg")

                val pd = ProgressDialog(this)
                pd.setMessage("Uploading...")
                pd.setCancelable(false)
                pd.show()

                val uploadTask = storageRef.putFile(imageUri)
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        // Update Firebase DB
                        databaseReference.child("users").child(mobile!!)
                            .child("profile_picture")
                            .setValue(imageUrl)

                        // Update UI
                        Picasso.get().load(imageUrl)
                            .placeholder(R.drawable.chatapp)
                            .into(userProfilePicture)

                        pd.dismiss()
                        showPopup("Profile Updated", R.drawable.chatapp)
                    }
                }.addOnFailureListener {
                    pd.dismiss()
                    showPopup("Upload Failed", R.raw.error)
                }
            }
        }
    }
}




//package com.example.chatapplication
//
//import android.app.ProgressDialog
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.chatapplication.message.MessagesAdapter
//import com.example.chatapplication.message.MessagesList
//import com.google.firebase.database.*
//import com.squareup.picasso.Picasso
//import de.hdodenhof.circleimageview.CircleImageView
//
//class MainActivity : AppCompatActivity() {
//
//
//    private val messageLists = ArrayList<MessagesList>()
//    private var mobile: String? = null
//    private var email: String? = null
//    private var name: String? = null
//    private val lastMessage :String ="";
//    private val unseenMessages = 0;
//    private val chatKey :String = "";
//    private val dataSet: boolean = true;
//    private lateinit var messageRecycleView: RecyclerView
//    private lateinit var userProfilePicture: CircleImageView
//    private lateinit var databaseReference: DatabaseReference
//    private lateinit var progressDialog: ProgressDialog
//    private lateinit var MessagesAdapter messagesAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//
//        // Init views
//        messageRecycleView = findViewById(R.id.messageRecycleView)
//        userProfilePicture = findViewById(R.id.userProfilePicture)
//
//        // Firebase reference
//        databaseReference = FirebaseDatabase
//            .getInstance()
//            .getReferenceFromUrl("https://chat-application-d6214-default-rtdb.firebaseio.com/")
//
//        // ProgressDialog
//        progressDialog = ProgressDialog(this)
//        progressDialog.setCancelable(false)
//        progressDialog.setMessage("Loading.....")
//        progressDialog.show()
//
//        // Intent se data receive karna
//        mobile = intent.getStringExtra("mobile")
//        email = intent.getStringExtra("email")
//        name = intent.getStringExtra("name")
//
//        // RecyclerView setup
//        messageRecycleView.setHasFixedSize(true)
//        messageRecycleView.layoutManager = LinearLayoutManager(this)
//        messageRecycleView.adapter =
//            MessagesAdapter(messageLists, this@MainActivity)
//
//        MessagesAdapter = MessagesAdapter(new MessagesAdapter(messageLists,MainActivity.this));
//        messageRecycleView.setAdrapter(MessagesAdapter);
//        // Debug print
//        println("Mobile: $mobile, Email: $email, Name: $name")
//
//        // Firebase se profile picture fetch
//        mobile?.let { mob ->
//            databaseReference.child("users").child(mob)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val profilePictureUrl =
//                            snapshot.child("profile_picture").getValue(String::class.java)
//
//                        if (!profilePictureUrl.isNullOrEmpty()) {
//                            Picasso.get()
//                                .load(profilePictureUrl)
//                               .placeholder(R.drawable.chatapp) // agar URL empty ho to default image
//                                .into(userProfilePicture)
//                        }
//
//                        progressDialog.dismiss()
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        progressDialog.dismiss()
//                        println("Firebase Error: ${error.message}")
//                    }
//                })
//        } ?: run {
//            progressDialog.dismiss()
//            println("Mobile null hai, Firebase fetch nahi hoga")
//        }
//
//        // ✅ Sab users list fetch karna
//        databaseReference.child("users")
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    messageLists.clear()
//                    unseenMessages = 0;
//                    lastMessage = "";
//                    chatKey = "";
//                    for (dataSnapshot in snapshot.children) {
//                        val getMobile = dataSnapshot.key
//                        dataSet = false;
//                        if (getMobile != null && getMobile != mobile) {
//                            val getName = dataSnapshot.child("name").getValue(String::class.java)
//                            val getProfilePicture =
//                                dataSnapshot.child("profile_picture").getValue(String::class.java)
//
//                            databaseReference.child("chat").addListenerForSingleValueEvent(
//                                new ValueEventListener(){
//                                     @Override
//                                     public void onDataChange(@NonNull DataSnapshot snapshot){
//                                         int getChatCounts = (int)snapshot.getChildrenCount();
//
//                                         if(getChatCounts >0){
//                                             for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
//                                                 final String getKey = dataSnapshot.getkey();
//                                                 chatKey =  getKey;
//                                                 if(dataSnapshot.hasChild("user_1")&& dataSnapshot.hasChild("user_2")&& dataSnapshot.hasChild("mesage")
//                                                 ){
//                                                     final String getUserOne = dataSnapshot.child("user_1").getValue(String.class);
//                                                     final String getUserTwo =  dataSnapshot.child("user_2").getValue(String.class);
//
//                                                     if ((getUserOne.equals(getMobile) && getUserTwo.equals(mobile))|| getUserOne.equals(mobile) && getUserTwo.equals(getMobile)))
//
//                                                     for (DataSnapshot chatDataSnapsnap : dataSnapshot1.child("messages".getChildren()){
//                                                         final long getMessageKey = Long.parseLong(chatDataSnapshot.getKey());
//                                                         final long getLastSeenMessage = Long.ParseLong(MemoryData.getLastMassageTS(MainActivity.this,getKey));
//
//                                                         lastMessage = chatDataSnapshot.chile("msg").getValue(String.class);
//
//                                                         if(getMessageKey > getLastSeenMessage){
//                                                             unseenMessages++;
//                                                         }
//                                                 }
//
//
//                                             }
//                                             }
//                                         }
//                                     }
//
//                                    @Overridepublic void onCancelled (@NonNull DatabaseError error){
//
//                                    }
//                                })
//                               if(dataSet){
//
//                                   val messageList = MessagesList(
//                                       name = getName ?: "Unknown",
//                                       mobile = getMobile,
//                                       "",
//                                       profilePicture = getProfilePicture ?: "",
//                                       0,
//                                       chatKey
//                                   )
//                                   messageLists.add(messageList)
//                                   MessagesAdapter.updateData(messageLists);
//
//                               }
//
//                               }
//                    }
//
//                     }
//
//                override fun onCancelled(error: DatabaseError) {
//                    println("Firebase Error: ${error.message}")
//                }
//            })
//    }
//}