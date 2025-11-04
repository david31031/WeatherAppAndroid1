package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    //Data Storage
    private val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val minTemps = ArrayList<Int>()
    private val maxTemps = ArrayList<Int>()
    private val conditions = ArrayList<String>()
    //Track which day we are entering(0=monday,1=tuesday,etc)
    private var currentDayIndex= 0
// ---Views---
    private lateinit var minTempEditText: EditText
    private lateinit var maxTempEditText: EditText
    private lateinit var conditionEditText: EditText
    private lateinit var addDataButton: Button
    private lateinit var clearAllButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var averageTempTextView: TextView
    private lateinit var daysListView: ListView
    // This Adapter Connects our data to the ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val displayList = ArrayList<String>()
    //this list will be shown in the listview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
            // find all views from the layout file
            minTempEditText = findViewById(R.id.minTempEditText)
            maxTempEditText = findViewById(R.id.maxTempEditText)
            conditionEditText = findViewById(R.id.conditionEditText)
            addDataButton = findViewById(R.id.addDataButton)
            clearAllButton = findViewById(R.id.clearAllButton)
            errorTextView = findViewById(R.id.errorTextView)
            averageTempTextView = findViewById(R.id.averageTempTextView)
            daysListView = findViewById(R.id.daysListView)
            // set up the ListView
            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
            daysListView.adapter = adapter
            // set on-click listener
            addDataButton.setOnClickListener {
                processDataInput()
            }
            clearAllButton.setOnClickListener {
                clearAllData()
            }
            // handle clicking on an item in the ListView
            daysListView.setOnItemClickListener { parent, view, position, id ->
                // create intent to start DetailActivity
                val intent = Intent(this, DetailActivity::class.java)
                // pass data to DetailActivity
                intent.putExtra("DAY_NAME", days[position])
                intent.putExtra("MIN_TEMP", minTemps[position])
                intent.putExtra("MAX_TEMP", maxTemps[position])
                intent.putExtra("CONDITION", conditions[position])
                // start DetailActivity
                startActivity(intent)
            }
            // set initial button text
            updateAddDataButtonText()
    }
    private fun processDataInput() {
        // (Requirement:Logging)
        Log.i("MainActivity", "Add Data button clicked.")
        //Get text from EditTexts
        val minTempStr = minTempEditText.text.toString()
        val maxTempStr = maxTempEditText.text.toString()
        val conditionStr = conditionEditText.text.toString()

        // (Requirement:Error Handling)
        if (minTempStr.isEmpty() || maxTempStr.isEmpty() || conditionStr.isEmpty()) {
            errorTextView.text = "Error: All Fields are Required."
            Log.e("InputError","User left one or more fields empty.")
            return
        }

        try{
            // convert text to numbers
            val minTemp = minTempStr.toInt()
            val maxTemp = maxTempStr.toInt()
            //check for logical errors
            if (minTemp > maxTemp ) {
                errorTextView.text = "Error: Min Temp cannot be greater than Max Temp."
                Log.e("InputError","User entered min > max..")
                return
            }
            // add data to lists
            minTemps.add(minTemp)
            maxTemps.add(maxTemp)
            conditions.add(conditionStr)
            // update display list
            displayList.add("${days[currentDayIndex]}: Min $minTemp, Max $maxTemp, Condition $conditionStr")
            // update ListView
            adapter.notifyDataSetChanged()
            // clear EditTexts
            minTempEditText.text.clear()
            maxTempEditText.text.clear()
            conditionEditText.text.clear()
            // update error text
            errorTextView.text = ""
            // move to next day
            currentDayIndex++
            if (currentDayIndex< days.size) {
                updateAddDataButtonText()
            } else {
                addDataButton.text = "All data entered"
                addDataButton.isEnabled = false
                minTempEditText.isEnabled = false
                maxTempEditText.isEnabled = false
                conditionEditText.isEnabled = false
                // calculate and display average
                calculateAndDisplayAverage()
            }
        }  catch (e: NumberFormatException) {
            errorTextView.text =
                "Error: Invalid input. Please enter valid numbers for temperature."
            Log.e("InputError", "User entered invalid input..")
        }
    }
    private fun calculateAndDisplayAverage() {
        var totalDailyAverage = 0.0
        for (i in 0 until minTemps.size) {
            val dailyAverage = (minTemps[i] + maxTemps[i]) / 2.0
            totalDailyAverage += dailyAverage
        }
        val weeklyAverage = totalDailyAverage / minTemps.size
        averageTempTextView.text = String.format("Weekly Average: %.1f", weeklyAverage)
        Log.i("Calculation", "Weekly average calculated: $weeklyAverage")
    }
    // function to clear data and allow re-input
    private fun clearAllData() {
        minTemps.clear()
        maxTemps.clear()
        conditions.clear()
        displayList.clear()
        // reset UI elements
        adapter.notifyDataSetChanged()
        // clear ListView
        currentDayIndex = 0
        updateAddDataButtonText()
        errorTextView.text = ""
        averageTempTextView.text = "Weekly Average: -"
        // re-enable inputs
        addDataButton.isEnabled = true
        minTempEditText.isEnabled = true
        maxTempEditText.isEnabled = true
        conditionEditText.isEnabled = true
        // logging
        Log.w("DataReset", "All Data has been cleared by the user.")
    }
    private fun updateAddDataButtonText() {
        if (currentDayIndex < days.size) {
            addDataButton.text = "Add Data For ${days[currentDayIndex]}"
        }
    }
}
