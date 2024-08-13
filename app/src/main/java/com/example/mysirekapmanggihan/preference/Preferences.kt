package com.example.mysirekapmanggihan.preference

import android.content.Context
import android.content.SharedPreferences

class Preferences(context: Context) {
    private val TAG_STATUS = "status"
    private val TAG_LEVEL = "level"
    private val TAG_PHONE = "phone"
    private val TAG_APP = "app"

    private val pref: SharedPreferences =
        context.getSharedPreferences(TAG_APP, Context.MODE_PRIVATE)

    var prefStatus: Boolean
        get() = pref.getBoolean(TAG_STATUS, false) // Default value is false
        set(value) = pref.edit().putBoolean(TAG_STATUS, value).apply()

    var prefLevel: String?
        get() = pref.getString(TAG_LEVEL, "") // Default value is ""
        set(value) = pref.edit().putString(TAG_LEVEL, value).apply()

    var prefPhone: String?
        get() = pref.getString(TAG_PHONE, "") // Default value is ""
        set(value) = pref.edit().putString(TAG_PHONE, value).apply()

//    var prefName: String?
//        get() = pref.getString(TAG_NAME, "") // Default value is ""
//        set(value) = pref.edit().putString(TAG_NAME, value).apply()
//
//    var prefAlamat: String?
//        get() = pref.getString(TAG_ALAMAT, "") // Default value is ""
//        set(value) = pref.edit().putString(TAG_ALAMAT, value).apply()

    // Function to clear specific preferences
    fun prefClear() {
        pref.edit().remove(TAG_STATUS).apply()
        pref.edit().remove(TAG_LEVEL).apply()
        pref.edit().remove(TAG_PHONE).apply()
//        pref.edit().remove(TAG_NAME).apply()
//        pref.edit().remove(TAG_ALAMAT).apply()
    }
}