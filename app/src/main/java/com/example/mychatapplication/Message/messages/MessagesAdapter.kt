package com.example.mychatapplication.Message.messages


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import com.example.mychatapplication.R
import com.example.mychatapplication.Message.chat.Chat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

// Adapter class RecyclerView ke liye jo messages list show karega
class MessagesAdapter(
    private var messageLists: List<MessagesList>,  // Message list data
    private val context: Context                   // Activity/Fragment ka context
) : RecyclerView.Adapter<MessagesAdapter.MyViewHolder>() {

    // ViewHolder create karne ke liye inflate karte hain layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_adapter_layout, parent, false)
        return MyViewHolder(view)


    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val list2 = messageLists[position]


        // Profile Picture
        if (list2.profilePicture.isNotEmpty()) {
            Picasso.get().load(list2.profilePicture).into(holder.profilePicture)
        }


        // ✅ Always fetch latest name from Firebase using mobile
        val dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("users").child(list2.mobile).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        holder.name.text = snapshot.value.toString()
                    } else {
                        holder.name.text = list2.name // fallback
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })


        // Last Message
        holder.lastMessage.text = list2.lastMessage

        // Unseen Messages
        if (list2.unseenMessage == 0) {
            holder.unSeenMessages.visibility = View.GONE
            holder.lastMessage.setTextColor(Color.parseColor("#959595")) // Grey
        } else {
            holder.unSeenMessages.visibility = View.VISIBLE
            holder.unSeenMessages.text = list2.unseenMessage.toString()
            holder.lastMessage.setTextColor(context.getColor(R.color.purple))
        }

        // On Click -> Open Chat
        holder.rootLayout.setOnClickListener {
            val intent = Intent(context, Chat::class.java)
            intent.putExtra("mobile", list2.mobile)
            intent.putExtra("name", holder.name.text.toString()) // ✅ latest name bhejo
            intent.putExtra("profilePicture", list2.profilePicture)
            intent.putExtra("chat_key", list2.chatKey)
            context.startActivity(intent)
        }
    }


    // RecyclerView ke items count
    override fun getItemCount(): Int = messageLists.size

    // Data update karne ke liye method
    fun updateData(newMessageLists: List<MessagesList>) {
        this.messageLists = newMessageLists
        notifyDataSetChanged()
    }

    // ViewHolder class jo har row ke views hold karegi
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val profilePicture: CircleImageView = itemView.findViewById(R.id.profilePicture)
        val name: TextView = itemView.findViewById(R.id.name)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        val unSeenMessages: TextView = itemView.findViewById(R.id.unSeenMessages)
        val rootLayout: View = itemView // pura row layout ko clickable banane ke liye

    }
}


//package com.example.chatapplication.message
//
//import android.content.Context
//import android.content.Intent
//import android.graphics.Color
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import de.hdodenhof.circleimageview.CircleImageView
//import com.example.chatapplication.R
//
//import com.squareup.picasso.Picasso
//
//class MessagesAdapter(
//    private val messageLists: List<MessagesList>,
//    private val Context: Context,
//
//
//) : RecyclerView.Adapter<MessagesAdapter.MyViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.message_adapter_layout, parent, false)
//        return MyViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        val list2 = messageLists[position]
//
//        // Profile Picture
//        if (list2.profilePicture.isNotEmpty()) {
//            Picasso.get().load(list2.profilePicture).into(holder.profilePicture)
//        }
//
//        // Name
//        holder.name.text = list2.name
//
//        // Last Message
//        holder.lastMessage.text = list2.lastMessage
//
//        // Unseen Messages
//        if (list2.unseenMessage == 0) {
//            holder.unSeenMessages.visibility = View.GONE
//            holder.lastMessage.setTextColor(Color.parseColor("#959595"));
//
//        } else {
//            holder.unSeenMessages.visibility = View.VISIBLE
//            holder.unSeenMessages.setText(list2.getUnseenMessage() + "");
//            holder.lastMessage.setTextColor(context.getResources().getColor(R.color.purple));
//
//            holder.unSeenMessages.text = list2.unseenMessage.toString()
//        }
//        holder.rootLayout.setOnClickListener (new View.OnClickListener(){
//            @Override
//            public void onClick(View v){
//                Intent intent = new Intent(context,Chat.class);
//                intent.putExtra("mobile",list2.getMobile());
//                intent.putExtra("name",list2.getName());
//                intent.putExtra("profile_pic",list2.profilePicture)
//                intent.putExtra("chat_key",list2.getChatKey());
//                Context.startActivity(intent);
//            }
//        })
//    }
//
//    public void updateData(List<MessageList> messageLists){
//        this.messageLists = messageLists;
//        notifyDataSetChanged();
//    }
//
//    override fun getItemCount(): Int = messageLists.size
//
//    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val profilePicture: CircleImageView = itemView.findViewById(R.id.profilePicture)
//        val name: TextView = itemView.findViewById(R.id.name)
//        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
//        val unSeenMessages: TextView = itemView.findViewById(R.id.unSeenMessages)
//        val  rootLayout : TextView = itemView.findViewById(R.id.unSeenMessages)
//    }
//}


//    // Har item (row) ke liye data bind karte hain
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        val list2 = messageLists[position]
//
//        // Profile Picture set karna agar available hai
//        if (list2.profilePicture.isNotEmpty()) {
//            Picasso.get().load(list2.profilePicture).into(holder.profilePicture)
//        }
//
//        // Name set karna
//        holder.name.text = list2.name
//
//        // Last message set karna
//        holder.lastMessage.text = list2.lastMessage
//
//        // Agar unseen message nahi hai to hidden rakhen
//        if (list2.unseenMessage == 0) {
//            holder.unSeenMessages.visibility = View.GONE
//            holder.lastMessage.setTextColor(Color.parseColor("#959595")) // Grey text
//        } else {
//            // Agar unseen message hai to count show karo
//            holder.unSeenMessages.visibility = View.VISIBLE
//            holder.unSeenMessages.text = list2.unseenMessage.toString()
//            holder.lastMessage.setTextColor(context.getColor(R.color.purple))
//        }
//
//        // Root layout par click listener (jab user kisi message par click kare)
//        holder.rootLayout.setOnClickListener {
//            val intent = Intent(context, Chat::class.java)
//            intent.putExtra("mobile", list2.mobile)
//            intent.putExtra("name", list2.name)
//            intent.putExtra("profile_pic", list2.profilePicture)
//            intent.putExtra("chat_key", list2.chatKey)
//            context.startActivity(intent)
//        }
//    }