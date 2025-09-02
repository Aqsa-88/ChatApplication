package com.example.mychatapplication.Message.messages




data class MessagesList(
    var name: String,
    var mobile: String,
    var lastMessage: String,
    var profilePicture: String,
    var unseenMessage: Int,
    val senderId: String = "",
    val receiverId: String = "",
    var chatKey : String,
    var seen: Boolean = false
)




//class MessagesList {
//    var name: String
//    var mobile: String
//    var lastMessage: String
//    var unseenMessage: Int
//    var profilePicture : String
//
//    constructor(name: String, mobile: String, lastMessage: String,profilePicture:String,unseenMessage: Int) {
//        this.name = name
//        this.mobile = mobile
//        this.lastMessage = lastMessage
//        this.profilePicture = profilePicture
//        this.unseenMessage = unseenMessage
//    }
//
//    fun getName(): String = name
//    fun setName(newName: String) { name = newName }
//
//    fun getMobile(): String = mobile
//    fun setMobile(newMobile: String) { mobile = newMobile }
//
//    fun getLastMessage(): String = lastMessage
//    fun setLastMessage(newLast: String) { lastMessage = newLast }
//
//    fun getUnseenMessage(): Int = unseenMessage
//    fun setUnseenMessage(count: Int) { unseenMessage = count }
//    fun profilePicture ():String = profilePicture
//}