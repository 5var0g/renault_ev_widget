package com.example.renault_app

import android.content.Context
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
            val apiResponse = module.callAttr("get_stat", sharedPreferences.getString("userName", ""), sharedPreferences.getString("password", ""), sharedPreferences.getString("accountId", ""), sharedPreferences.getString("vin", "")).toString()

            val jObject = JSONObject("{$apiResponse}")
            batteryLevel = jObject.getString("batteryLevel")
            batteryAutonomy = jObject.getString("batteryAutonomy")
            plugStatus = jObject.getString("plugStatus")
            chargingStatus = jObject.getString("chargingStatus")
            timestamp = jObject.getString("timestamp")
            chargingRemainingTime = jObject.getInt("chargingRemainingTime")
            chargingInstantaneousPower = jObject.getInt("chargingInstantaneousPower")

            timestamp = timestamp.replace("T", " ")
            timestampShort = formatDateFromString("HH:mm dd.MM.", timestamp)

        } catch (e: PyException){
            e.printStackTrace()
        }

        nextTimeRunAllowed = (System.currentTimeMillis() / 1000) + 10
    }
}

fun connectRenault(context: Context, username: String, password: String): String {
    if (!Python.isStarted()) {
        Python.start(AndroidPlatform(context))
    }
    val py = Python.getInstance()
    val module = py.getModule("getStat")

    try {
        return module.callAttr("get_key", username, password).toString()
    } catch (e: PyException) {
        e.printStackTrace()
    }

    return "err"
}

fun getVehicles(context: Context, username: String, password: String, accountId: String): String {
    if (!Python.isStarted()) {
        Python.start(AndroidPlatform(context))
    }
    val py = Python.getInstance()
    val module = py.getModule("getStat")

    try {
        return module.callAttr("get_vehicles", username, password, accountId).toString()
    } catch (e: PyException) {
        e.printStackTrace()
    }

    return "err"
}

private fun formatDateFromString(outputFormat: String?, inputDate: String?): String {
        val parsed: Date?
        var outputDate = ""
        val dfInput = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        val dfOutput = SimpleDateFormat(outputFormat, Locale.getDefault())
        try {
            parsed = inputDate?.let { dfInput.parse(it) }
            val calendar = Calendar.getInstance()
            if (parsed != null) {
                calendar.time = parsed
            }
            calendar.add(Calendar.HOUR, 1)
            outputDate = dfOutput.format(calendar.time)
        } catch (e: ParseException) {
        }
        return outputDate
}

private fun getCurrentTime(): String {
    val format = SimpleDateFormat("HH:mm")

    return format.format(Date())
}