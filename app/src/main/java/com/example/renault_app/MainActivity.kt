package com.example.renault_app

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("credentials", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val userNameEdt = findViewById<EditText>(R.id.idEdtUserName)
        val passwordEdt = findViewById<EditText>(R.id.idEdtPassword)
        val button = findViewById<Button>(R.id.idBtn)
        val spinner = findViewById<Spinner>(R.id.idVinSpinner)
        var vinList: Array<String> = arrayOf("0")

        userNameEdt.setText(sharedPreferences.getString("userName", null))

        if(sharedPreferences.getBoolean("connected", false)){
            button.text = "Disconnect"

            val vehicleListString = sharedPreferences.getString("vehicles", "")
            if(vehicleListString != ""){
                vinList = createListFromString(vehicleListString!!)
                populateSpinnerVinList(vinList, sharedPreferences.getString("vin", "")!!)
                spinner.visibility = VISIBLE
            }
        }

        button.setOnClickListener {
            if (sharedPreferences.getBoolean("connected", false)) {
                editor.putString("userName", "")
                editor.putString("password", "")
                editor.putString("accountId", "")
                editor.putString("vehicles", "")
                editor.putBoolean("connected", false)
                editor.commit()
                button.text = "Connect"
                spinner.visibility = GONE

            } else {
                val userName: String = userNameEdt.text.toString()
                val password: String = passwordEdt.text.toString()

                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter user name and password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    editor.putString("userName", userName)
                    editor.putString("password", password)

                    val accountId = connectRenault(this, userName, password)
                    if (accountId == "err") {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to connect!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else {
                        val vehicles = getVehicles(this, userName, password, accountId)
                        if (accountId == "err") {
                            Toast.makeText(
                                this@MainActivity,
                                "Failed to get vehicle list!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else {
                            editor.putString("accountId", accountId)
                            editor.putString("vehicles", vehicles)
                            editor.putBoolean("connected", true)
                            editor.commit()

                            vinList = createListFromString(vehicles)

                            populateSpinnerVinList(vinList, sharedPreferences.getString("vin", "")!!)
                            spinner.visibility = VISIBLE

                            button.text = "Disconnect"
                        }
                    }
                }
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                editor.putString("vin", vinList[position])
                editor.commit()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val intent = Intent(this, BatteryWidget::class.java)
        intent.action = "android.appwidget.action.APPWIDGET_UPDATE"
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(
                application,
                BatteryWidget::class.java
            )
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    private fun createListFromString(list: String): Array<String>{
        var newList: String = list

        newList = newList.replace("[", "")
        newList = newList.replace("]", "")
        newList = newList.replace("'", "")
        newList = newList.replace(" ", "")

        return newList.split(",").toTypedArray()
    }

    private fun populateSpinnerVinList(vinList: Array<String>, selectedVin: String) {
        var spinner: Spinner = findViewById(R.id.idVinSpinner)
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vinList)
        spinner.adapter = arrayAdapter

        vinList.forEachIndexed { index, vin ->
            if (vin == selectedVin) {
                spinner.setSelection(index)
            }
        }
    }
}