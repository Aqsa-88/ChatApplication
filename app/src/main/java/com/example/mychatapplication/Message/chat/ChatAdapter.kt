package com.example.mychatapplication.Message.chat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mychatapplication.Message.MemoryData
import com.example.mychatapplication.R

class ChatAdapter(
    private var chatLists: List<ChatList>,
    private val context: Context,
    private val currentUserMobile: String // current user mobile passed from Chat activity
) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_adapter_layput, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chat = chatLists[position]
        val senderMobile = chat.mobile.trim().lowercase()
        val currentUserMobile = MemoryData.getUserMobile(context).trim().lowercase()


        Log.d("ChatAdapter", "sender: $senderMobile | current: $currentUserMobile")

        // Reset dono layouts invisible karke start karo
        holder.myLayout.visibility = View.GONE
        holder.oppoLayout.visibility = View.GONE

        if (senderMobile == currentUserMobile) {
            // Sent message (Right Side)
            holder.myLayout.visibility = View.VISIBLE
            holder.myMessage.text = chat.message ?: ""
            holder.myTime.text = "${chat.date} ${chat.time}"
            holder.myMessage.setBackgroundResource(R.drawable.my_msg_back)
            holder.mySeenStatus.visibility = View.GONE
            // ✅ Seen status show karo
            if (chat.seen) {
                holder.mySeenStatus.visibility = View.VISIBLE
                holder.mySeenStatus.text = "✓✓ Seen"
            } else {
                holder.mySeenStatus.visibility = View.VISIBLE
                holder.mySeenStatus.text = "✓ Sent"
            }
        } else {
            // Received message (Left Side)
            holder.oppoLayout.visibility = View.VISIBLE
            holder.oppoMessage.text = chat.message ?: ""
            holder.oppoTime.text = "${chat.date} ${chat.time}"
            holder.oppoMessage.setBackgroundResource(R.drawable.opo_msg_back)
        }
    }

    override fun getItemCount(): Int = chatLists.size

    fun updateChatList(newChatLists: List<ChatList>) {
        chatLists = newChatLists
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val oppoLayout: LinearLayout = itemView.findViewById(R.id.oppoLayout)
        val myLayout: LinearLayout = itemView.findViewById(R.id.myLayout)

        val oppoMessage: TextView = itemView.findViewById(R.id.oppoMessage)
        val oppoTime: TextView = itemView.findViewById(R.id.oppoMsgTime)

        val myMessage: TextView = itemView.findViewById(R.id.myMessage)
        val myTime: TextView = itemView.findViewById(R.id.myMsgTime)
        val mySeenStatus: TextView = itemView.findViewById(R.id.mySeenStatus)
    }
}




//package com.example.chatapplication.chat
//
//import androidx.annotation.NonNull
//
//class ChatAdapter extend RecyclerView.Adapter<ChatAdapter.MyViewHolder>{
//  private final List<ChatList>chatLists
//          private final context Context;
//    private String userMobile;
//    public ChatAdapter(List<ChatList> chatlist , context Context){
//        this.chatLists = chatLists
//        this.context = context
//        this.userMobile = MemoryData.getData(context)
//    }
//
//    @NonNull
//    @Override
//
//            public ChatAdapter.MyViewHolder onCreate (@NonNull ViewGroup PARENT,INT viewType)
//            {
//                private LinearLayput oppoLayout,myLayout;
//                private TextView oppoMessage,myMessage;
//                private TextView oppoTime ,myTime;
//                public MyViewHolder(@NonNull view itemView){
//                    super(itemView);
//                    oppolayout = item.findViewById(R.id.oppoLayput)
//                    myLayout = item.findViewById(R.id.myLayout)
//                    oppoMessage = item.findViewById(R.id.oppoMessage)
//                    myMessage = item.findViewById(R.id.myMessage)
//                    oppoTime = item.findViewById(R.id.oppoTime)
//                    myTime = item.findViewById(R.id.myTime)
//                }
//                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_adapter_layout,null));
//
//
//            }
//    @void onBindViewHolder(@NonNull  ChatAdapter.MyViewHolder holder,int position){
//     ChatList list2 = chatLists.get(position);
//        if(list2.getMobile().equals(userMobile)){
//            holder.myLayout.setVisibility(View.VISIBLE);
//            holder.oppoLayout.setVisibility(View.GONE);
//          holder.myMessage.settext(list2.getgetMessage());
//         holder.myTime.settext(list2.getDate()+" "+ list2.gettime());
//        }
//        else{
//            holder.myLayout.setVisibility(View.GONE);
//            holder.oppoLayout.setVisibility(View.VISIBLE);
//            holder.oppoMessage.settext(list2.getgetMessage())
//            holder.oppoTime.setText(list2.getDate()+" "+ list2.gettime());
//
//        }
//    }
//    @Overridepubliv int getItemCOunt(){
//        return chatLists.size();
//    }
//    public void updateChatList(List<ChatList>chatLists){
//    this.chatLists = chatLists;
//    }
//
//  static class MyViewHolder extends RecyclerView.ViewHolder {
//      public MyViewHolder(@NonNull View itemView){
//          super(itemView)
//      }
//    }
//}