package com.example.mychatapplication.Message.chat

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mychatapplication.Message.MemoryData
import com.example.mychatapplication.R
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Chat : AppCompatActivity() {

    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance()
        .getReferenceFromUrl("https://mychatapplication-ffdb9-default-rtdb.firebaseio.com/")

    private lateinit var chattingRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatLists = ArrayList<ChatList>()

    private var chatKey = ""
    private var currentUserMobile = ""
    private var otherUserMobile = ""
    private var otherUserName = ""
    private var lastSeenTimestamp: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        chattingRecyclerView = findViewById(R.id.chattingRecycleView)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        chattingRecyclerView.layoutManager = layoutManager

        chatAdapter = ChatAdapter(chatLists, this,currentUserMobile)
        chattingRecyclerView.adapter = chatAdapter
        currentUserMobile = MemoryData.getUserMobile(this) ?: ""

        val backBtn: ImageView = findViewById(R.id.backBtn)
        val nameTV: TextView = findViewById(R.id.name)
        val messageBox: EditText = findViewById(R.id.messageEditTxt)
        val profilePicture: CircleImageView = findViewById(R.id.profilePicture)
        val sendBtn: ImageView = findViewById(R.id.sendBtn)

        // ✅ Intent data
        otherUserName = intent.getStringExtra("name") ?: ""
        val getProfilePic = intent.getStringExtra("profilePicture")
        otherUserMobile = intent.getStringExtra("mobile") ?: ""

        // ✅ Current user
        currentUserMobile = MemoryData.getUserMobile(this) ?: ""

        // ✅ Fix chatKey
        chatKey = if (currentUserMobile < otherUserMobile)
            "$currentUserMobile-$otherUserMobile"
        else
            "$otherUserMobile-$currentUserMobile"

        Log.d("ChatDebug", "ChatKey: $chatKey")

        nameTV.text = otherUserName
        if (!getProfilePic.isNullOrEmpty()) {
            Picasso.get().load(getProfilePic).into(profilePicture)
        }

        // ✅ Mark all messages as seen
        dbRef.child("chat").child(chatKey).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (msgSnap in snapshot.children) {
                        val receiver = msgSnap.child("receiver").getValue(String::class.java)
                        val seen = msgSnap.child("seen").getValue(Boolean::class.java) ?: false
                        if (receiver == currentUserMobile && !seen) {
                            msgSnap.ref.child("seen").setValue(true)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // ✅ Load lastSeen
        dbRef.child("users").child(currentUserMobile).child("lastSeen")
            .get()
            .addOnSuccessListener { lastSeenTimestamp = it.getValue(Long::class.java) ?: 0 }

        // ✅ Listen messages
        dbRef.child("chat").child(chatKey).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatLists.clear()
                    for (msgSnap in snapshot.children) {
                        val msg = msgSnap.child("msg").getValue(String::class.java) ?: ""
                        val sender = msgSnap.child("sender").getValue(String::class.java) ?: ""
                        val receiver = msgSnap.child("receiver").getValue(String::class.java) ?: ""
                        val timestamp = msgSnap.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                        val date = msgSnap.child("date").getValue(String::class.java) ?: ""
                        val time = msgSnap.child("time").getValue(String::class.java) ?: ""
                        val seen = msgSnap.child("seen").getValue(Boolean::class.java) ?: false

                        if (msg.isNotEmpty()) {
                            val unseenCount =
                                if (sender != currentUserMobile && !seen) 1 else 0

                            chatLists.add(
                                ChatList(
                                    mobile = sender,
                                    name = if (sender == currentUserMobile) "Me" else otherUserName,
                                    message = msg,
                                    date = date,
                                    time = time,
                                    unseenCount = unseenCount
                                )
                            )
                        }
                    }
                    chatAdapter.updateChatList(chatLists)
                    chattingRecyclerView.scrollToPosition(chatLists.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatDebug", "Failed to load messages: ${error.message}")
                }
            })

        // ✅ Send message
        sendBtn.setOnClickListener {
            val message = messageBox.text.toString().trim()
            if (message.isNotEmpty()) {
                val messageKey = dbRef.child("chat").child(chatKey).child("messages").push().key!!
                val timestamp = System.currentTimeMillis()
                val messageMap = hashMapOf<String, Any>(
                    "msg" to message,
                    "sender" to currentUserMobile,
                    "receiver" to otherUserMobile,
                    "timestamp" to timestamp,
                    "date" to android.text.format.DateFormat.format("dd-MM-yyyy", timestamp).toString(),
                    "time" to android.text.format.DateFormat.format("hh:mm a", timestamp).toString(),
                    "seen" to false
                )

                dbRef.child("chat").child(chatKey).child("messages").child(messageKey)
                    .setValue(messageMap)
                    .addOnSuccessListener {
                        messageBox.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Message send failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // ✅ Back button
        backBtn.setOnClickListener {
            dbRef.child("users").child(currentUserMobile).child("lastSeen")
                .setValue(System.currentTimeMillis())
            finish()
        }
    }
}



//package com.example.chatapplication.chat
//
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.example.chatapplication.R
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.FirebaseDatabase
//import java.text.SimpleDateFormat
//
//class Chat : AppCompatActivity() {
//    databaseReference = FirebaseDatabase
//    .getInstance()
//    .getReferenceFromUrl("https://chat-application-d6214-default-rtdb.firebaseio.com/")
//
//    private string chatKey ;
//    string getUserMobile="";
//    private RecycleView chattingRecyclerView;
//    private final List<ChatList> chatLists = new ArrayList<>();
//private ChatAdapter chatAdapter;
//    private boolean loadingFirstTime = true;
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_chat)
//
//        final ImageView backBtn = findViewById(R.id.backBtn)
//        final TextView nameTV = findViewById(R.id.name)
//        final EditText messageEditText = findViewById(R.id.messageEditTxt)
//        final CircleImageView profilePicture = findViewById(R.id.backBtn)
//        final ImageView sendBtn = findViewById(R.id.sendBtn)
//        chattingRecyclerView = findViewById(R.id.chattingRecycleView)
////get string from message adapter class
//        final String getName = getIntent().getStringExtra("name");
//        final String profilePicture = getIntent().getStringExtra("profilePicture");
//        final String chatKey =  getIntent().getStringExtra("chat_key");
//        final String getMobile =  getIntent().getStringExtra("mobile");
////get user mobile from memory
//        getUserMobile = MemoryData.getData(chat.this)
//        nameTV.setText(getName);
//        Picasso.get().load(getprofilePicture).into(profilePicture);
//
//        chattingRecyclerView.setHasFIxedSize(true);
//        chattingRecyclerView.setLayoutManager(nwLinearLayoutmanager(Chat.this))
//
//   ChatAdapter = new ChatAdapter(chatLists,Chat.this);
//   chattingRecyclerView.setAdapter(chatAdapter);
//
//            databaseReference.addListenerForSingleValueEvent(new ValueEventListener(){
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot){
//                    if (chatkey.isEmpty()){
//                    int chatKey = 1;
//                    if (snapshot.hasChild("chat")){
//                        chatKey = String.valueof(snapshot.child("chat").getChildrenCount()+ 1;
//                    }
//                    }
//                    if(snapshot.hasChild("chat")){
//                        if (snapshot.child("child").child(chatKey).hasChild("messages")){
//                          chatLists.clear();
//                            for(DataSnapshot messageSnapshot : snapshot.child("chat").child(chatKey).child("messages").getChildren()){
//                                if(messageSnapshot.hasChild("msg")&& messageSnapshot.hasChild("mobile")){
//                                    final String messageTimestamos = messageSnapshot.getKey();
//                                    final String getMobile = messageSnapshot.chils("mobile").getValue(String.class);
//                                    final String getMsg = messageSnapshot.child("msg").getVlaue(String.class);
//                                    Timestamp timestamp = new Timestamp(Long.parseLong(messageTimestamps));
//                                    Date date = new Date(timestamp.getTime());
//                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy ",Locale.getDefault());
//                                    SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(" hh:mm:ss",Locale.getDefault());
//
//                                    ChatList chatList = new ChatList(getMobile,getName,getMsg,simpleDateFormat.format(date),simpleTimeFormate.formate(date));
//                                    chatLists.add(chatList);
//
//                                    if(loadingFirstTime || Long.parseLong(messageTimestamps)> Long.parseLong(MemoryDta.getLastMsgTS(Chat.this ,chatKey))){
//                                      loadingFirsttime = false;
//                                        MemoryData.savelastMsgTS(currentTimestamp,chatKey,Chat.this)
//
//                                   chatAdapter.updateChatList(chatLists);
//                                        chattingRecyclerView.scrollToPosition(chatLists.size() -1)
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            })
//
//        sendBtn.setOnClickListener(new View.OnClickListener(){
//            @Overridepublic void onclick(View v){
//
//                final String getTxtMessage =  messageEditText.getText().toString()
//                 fianl String currentTimestamp = String.valueof(System,currentTimeMillis()).substring(0,10);
////                MemoryData.savelastMsgTS(currentTimestamp,chatKey,Chat.this)
//                databaseReference.child("chat").child((chatKey).child("user1").setValue(getUserMobile))
//                databaseReference.child("chat").child((chatKey).child("user2").setValue(getMobile))
//                databaseReference.child("chat").child((chatKey).child("messages").child(currentTimestamp).child("msg").setValue(getTxtMessage))
//                databaseReference.child("chat").child((chatKey).child("messages").child(currentTimestamp).child("mobile").setValue(getUserMobile))
//
//            }
//        })
//        backBtn.setOnClickListener(new View.OnClickListener(){
//            @Overridepublic void onclick(View v){
//                finish();
//            }
//        })
//    }}