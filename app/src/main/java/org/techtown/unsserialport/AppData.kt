package org.techtown.unsserialport

import android.content.Context
import android.util.Log
import android.widget.Toast

object AppData {
    val logList by lazy { mutableListOf<String>() }

    const val ACTION_USB_PERMISSION = "org.techtown.serial.USB_PERMISSION"

    var isDebug = true
    fun debug(tag: String, msg: String) {
        if (isDebug) Log.d(tag, msg)
    }

    fun error(tag: String, msg: String) {
        if (isDebug) Log.e(tag, msg)
    }

    private lateinit var toast: Toast
    fun showToast(context: Context, msg: String) {
        if (::toast.isInitialized) toast.cancel()
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast.show()
    }

}