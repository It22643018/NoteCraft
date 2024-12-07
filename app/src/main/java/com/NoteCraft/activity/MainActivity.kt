package com.kikunote.activity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.NoteCraft.helper.SharedPreferencesHelper
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.kikunote.R
import com.kikunote.adapter.SectionsPagerAdapter
import com.kikunote.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var calendar: Calendar
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var stopwatchRunning = false
    private var timeInMilliseconds = 0L
    private var startTime = 0L
    private var timeSwapBuff = 0L
    private var updateTime = 0L
    private lateinit var stopwatchHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendar = Calendar.getInstance()

        sharedPreferencesHelper = SharedPreferencesHelper(this)  // Initialize SharedPreferencesHelper

        initView()
        initListener()

        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    it,
                    AppUpdateType.IMMEDIATE,
                    this,
                    999
                )
            }
        }

        // Timer/Stopwatch setup
        stopwatchHandler = Handler()
        val timerTextView = findViewById<TextView>(R.id.timerTextView)

        binding.startButton.setOnClickListener {
            if (!stopwatchRunning) {
                startTime = SystemClock.uptimeMillis()
                stopwatchHandler.postDelayed(updateTimer, 0)
                stopwatchRunning = true
                // Save stopwatch state in SharedPreferences
                sharedPreferencesHelper.saveNote("startTime", startTime.toString())
                sharedPreferencesHelper.saveNote("timeSwapBuff", timeSwapBuff.toString())
            }
        }

        binding.pauseButton.setOnClickListener {
            if (stopwatchRunning) {
                timeSwapBuff += timeInMilliseconds
                stopwatchHandler.removeCallbacks(updateTimer)
                stopwatchRunning = false
                // Save stopwatch state in SharedPreferences
                sharedPreferencesHelper.saveNote("timeSwapBuff", timeSwapBuff.toString())
            }
        }

        binding.resetButton.setOnClickListener {
            startTime = 0L
            timeSwapBuff = 0L
            updateTime = 0L
            stopwatchRunning = false
            timerTextView.text = "00:00:00"
            // Clear stopwatch data from SharedPreferences
            sharedPreferencesHelper.deleteNote("startTime")
            sharedPreferencesHelper.deleteNote("timeSwapBuff")
        }

        // Restore stopwatch state from SharedPreferences if available
        val savedStartTime = sharedPreferencesHelper.getNote("startTime")?.toLongOrNull() ?: 0L
        val savedTimeSwapBuff = sharedPreferencesHelper.getNote("timeSwapBuff")?.toLongOrNull() ?: 0L
        if (savedStartTime != 0L || savedTimeSwapBuff != 0L) {
            startTime = savedStartTime
            timeSwapBuff = savedTimeSwapBuff
        }
    }

    private val updateTimer: Runnable = object : Runnable {
        override fun run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime
            updateTime = timeSwapBuff + timeInMilliseconds
            val secs = (updateTime / 1000).toInt() % 60
            val mins = (updateTime / 1000 / 60).toInt() % 60
            val hrs = (updateTime / 1000 / 60 / 60).toInt()
            findViewById<TextView>(R.id.timerTextView).text = String.format("%02d:%02d:%02d", hrs, mins, secs)
            stopwatchHandler.postDelayed(this, 0)
        }
    }

    private fun showDateTimePicker(taskTitle: String, taskContent: String) {
        // DatePicker Dialog
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // TimePicker Dialog
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                setReminder(calendar, taskTitle, taskContent)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePickerDialog.show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.show()
    }

    private fun setReminder(calendar: Calendar, taskTitle: String, taskContent: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("TASK_TITLE", taskTitle)
            putExtra("TASK_CONTENT", taskContent)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        // Save reminder details to SharedPreferences
        sharedPreferencesHelper.saveNote("reminderTitle", taskTitle)
        sharedPreferencesHelper.saveNote("reminderContent", taskContent)

        Toast.makeText(this, "Reminder set for: ${calendar.time}", Toast.LENGTH_SHORT).show()
        Log.d("Reminder", "Task Title: $taskTitle, Task Content: $taskContent")
    }

    class ReminderReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Reminder"
            val taskContent = intent.getStringExtra("TASK_CONTENT") ?: "TIME UP!"

            Log.d("ReminderReceiver", "Received Task Title: $taskTitle, Task Content: $taskContent")

            val builder = NotificationCompat.Builder(context, "reminderChannel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(taskTitle)
                .setContentText(taskContent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            with(NotificationManagerCompat.from(context)) {
                notify(1, builder.build())
            }
        }
    }

    private fun initView() {
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.tabs.setupWithViewPager(view_pager)
    }

    private fun initListener() {
        binding.floatingActionButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.floatingActionButton -> {
                startActivity(Intent(this, EditActivity::class.java))
            }
        }
    }

    override fun onResume() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener {
                if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        it,
                        AppUpdateType.IMMEDIATE,
                        this,
                        999
                    )
                }
            }
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999 && resultCode == Activity.RESULT_OK) {
            // TODO: do something in here if in-app updates success
        } else {
            // TODO: do something in here if in-app updates failure
        }
    }
}
