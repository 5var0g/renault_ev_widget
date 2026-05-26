package com.example.renault_app

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.chaquo.python.PyException
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


var updated = "--:--"
var batteryLevel = "--"
var batteryAutonomy = "--"
var plugStatus = "0"
var chargingStatus = "0"
var timestamp = ""
var timestampShort = "--:-- --.--."
var chargingRemainingTime = 0
var chargingInstantaneousPower = 0
var nextTimeRunAllowed: Long = 0

fun plugStatus(plugStatus: String, chargingStatus: String): String {
    return when (plugStatus) {
        "1" -> {
            when (chargingStatus) {
                "0.0" -> "Plugged"
                "0.1" -> "Waiting"
                "0.2" -> "Charged"
                "0.3" -> "Waiting"
                "1.0" -> "Charging"
                "-1.0" -> "ERROR!"
                else -> ""
            }
        }
        "-1" -> "ERROR!"
        else -> ""
    }
}

fun getRenaultData(context: Context) {
    val tsLong = System.currentTimeMillis() / 1000

    if(tsLong > nextTimeRunAllowed) {
        updated = getCurrentTime()

        val sharedPreferences = context.getSharedPreferences("credentials", 0)
        if(!sharedPreferences.getBoolean("connected", false))return

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        val py = Python.getInstance()
        val module = py.getModule("getStat")

        try {
            val apiResponse = module.callAttr("get_stat",sharedPreferences.getString("token",""), sharedPreferences.getString("renaultId", ""), sharedPreferences.getString("vin", "")).toString()
            Log.e("testis", apiResponse)

            val jObject = JSONObject("{$apiResponse}")
            batteryLevel = jObject.getString("batteryLevel")
            batteryAutonomy = jObject.getString("batteryAutonomy")
            plugStatus = jObject.getString("plugStatus")
            chargingStatus = jObject.getString("chargingStatus")
            timestamp = jObject.getString("timestamp")
            chargingRemainingTime = jObject.getInt("chargingRemainingTime")

            Log.e("testis", jObject.getString("batteryLevel"))

            chargingInstantaneousPower = try {
                jObject.getInt("chargingInstantaneousPower")
            } catch (_: Exception) {
                0
            }

            timestamp = timestamp.replace("T", " ")
            timestampShort = formatDateFromString(timestamp)

        } catch (e: PyException){
            Log.e("testis", e.toString())
            e.printStackTrace()
        }

        nextTimeRunAllowed = (System.currentTimeMillis() / 1000) + 10
    }
}

fun connectRenault(context: Context, username: String?, password: String?): String {
    if (!Python.isStarted()) {
        Python.start(AndroidPlatform(context))
    }
    val py = Python.getInstance()
    val module = py.getModule("getStat")

    try {
        return module.callAttr("login", username, password).toString()
    } catch (e: PyException) {
        e.printStackTrace()
    }

    return "err"
}

fun getVehicles(context: Context, username: String, password: String, renaultId: String): String {
    if (!Python.isStarted()) {
        Python.start(AndroidPlatform(context))
    }
    val py = Python.getInstance()
    val module = py.getModule("getStat")

    try {
        return module.callAttr("get_vehicles", username, password, renaultId).toString()
    } catch (e: PyException) {
        e.printStackTrace()
    }

    return "err"
}

private fun formatDateFromString(inputDate: String?): String {
        val parsed: Date?
        var outputDate = ""
        val dfInput = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        val dfOutput = SimpleDateFormat("HH:mm dd.MM.", Locale.getDefault())
        try {
            parsed = inputDate?.let { dfInput.parse(it) }
            val calendar = Calendar.getInstance()
            if (parsed != null) {
                calendar.time = parsed
            }
            calendar.add(Calendar.HOUR, 1)
            outputDate = dfOutput.format(calendar.time)
        } catch (_: ParseException) {
        }
        return outputDate
}

@SuppressLint("SimpleDateFormat")
private fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm").format(Date())
}