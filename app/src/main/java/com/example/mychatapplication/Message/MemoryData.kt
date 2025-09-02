package com.example.mychatapplication.Message

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MemoryData {
    companion object {

        // File names constants
        private const val FILE_USER = "user_mobile.txt"
        private const val FILE_NAME = "nameee.txt"
        private const val FILE_DATA = "datatat.txt"

        /** ------------------ USER MOBILE ------------------ **/

        fun saveUserMobile(context: Context, mobile: String) {
            saveToFile(context, FILE_USER, mobile)
        }

        fun getUserMobile(context: Context): String {
            return readFromFile(context, FILE_USER, "")
        }

        fun clearUserMobile(context: Context) {
            context.deleteFile(FILE_USER)
        }

        /** ------------------ GENERIC DATA ------------------ **/

        fun saveData(context: Context, data: String) {
            saveToFile(context, FILE_DATA, data)
        }

        fun getData(context: Context): String {
            return readFromFile(context, FILE_DATA, "")
        }

        /** ------------------ LAST MESSAGE TIMESTAMP ------------------ **/

        fun saveLastMsgTS(context: Context, chatId: String, timestamp: String) {
            saveToFile(context, "${chatId}_lastMsgTS.txt", timestamp)
        }

        fun getLastMsgTS(context: Context, chatId: String): String {
            return readFromFile(context, "${chatId}_lastMsgTS.txt", "0")
        }

        /** ------------------ USER NAME ------------------ **/

        fun saveName(context: Context, name: String) {
            saveToFile(context, FILE_NAME, name)
        }

        fun getName(context: Context): String {
            return readFromFile(context, FILE_NAME, "")
        }

        /** ------------------ HELPER FUNCTIONS ------------------ **/

        // Save string into a file
        private fun saveToFile(context: Context, fileName: String, data: String) {
            try {
                context.openFileOutput(fileName, Context.MODE_PRIVATE).use { fos ->
                    fos.write(data.toByteArray())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        // Read string from a file
        private fun readFromFile(context: Context, fileName: String, defaultValue: String): String {
            return try {
                context.openFileInput(fileName).use { fis ->
                    val reader = BufferedReader(InputStreamReader(fis))
                    val content = reader.readText()
                    reader.close()
                    content
                }
            } catch (e: IOException) {
                defaultValue
            }
        }
    }
}



//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.Utils; // Is line mein 'Utils' galat hai, 'util' hona chahiye ya koi specific utility class
//
//public final class MemoryData {
//
//    // Data ko local file mein save karta hai
//    public static void saveData(String data, Context context) {
//        try {
//            // "datatat.txt" naam ki file ko private mode mein open karta hai
//            FileOutputStream fileOutputStream = context.openFileOutput("datatat.txt", Context.MODE_PRIVATE);
//            // String data ko bytes mein convert karke file mein likhta hai
//            fileOutputStream.write(data.getBytes());
//            // FileOutputStream ko close karta hai taaki resources free ho jaayen
//            fileOutputStream.close();
//        } catch (IOException e) {
//            // Agar file operations ke dauraan koi error aata hai, toh usko print karta hai
//            e.printStackTrace();
//        }
//    }
//
//    // Local file se data read karta hai
//    public static String getData(Context context) {
//        String data = ""; // Default empty string agar data na mile
//        try {
//            // "datatat.txt" naam ki file ko read karne ke liye open karta hai
//            FileInputStream fis = context.openFileInput("datatat.txt");
//            // FileInputStream ko InputStreamReader se wrap karta hai taaki characters read kiye jaa saken
//            InputStreamReader isr = new InputStreamReader(fis);
//            // BufferedReader se efficiency badhaata hai
//            BufferedReader bufferedReader = new BufferedReader(isr);
//            // Data ko store karne ke liye StringBuilder ka use karta hai
//            StringBuilder sb = new StringBuilder();
//            String line;
//            // Har line ko read karta hai jab tak null na mile (file ka end)
//            while ((line = bufferedReader.readLine()) != null) {
//                sb.append(line); // Read ki gayi line ko StringBuilder mein jodta hai
//            }
//            data = sb.toString(); // Poore data ko String mein convert karta hai
//            // BuffredReader, InputStreamReader, aur FileInputStream ko close karta hai
//            bufferedReader.close(); // Important: BuffredReader ko close karna zaruri hai
//            isr.close();
//            fis.close();
//        } catch (IOException e) {
//            // Agar file operations ke dauraan koi error aata hai, toh usko print karta hai
//            e.printStackTrace();
//        }
//        return data; // Read kiya gaya data wapas karta hai
//    }
//}