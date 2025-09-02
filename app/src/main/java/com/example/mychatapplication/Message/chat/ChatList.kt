package com.example.mychatapplication.Message.chat



// ChatList class ek model class hai jo ek single chat item ka data rakhegi
// isme user ka mobile, name, message, date aur time save hota hai
class ChatList(
    var mobile: String,   // User ka mobile number
    var name: String,     // User ka naam
    var message: String,  // Chat message
    var date: String,     // Message bhejne ki date
    var time: String,    // Message bhejne ka time
    var unseenCount: Int = 0
)
