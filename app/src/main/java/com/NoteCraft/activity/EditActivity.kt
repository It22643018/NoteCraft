package com.kikunote.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.awesomedialog.*
import com.kikunote.R
import com.kikunote.databinding.ActivityEditBinding
import com.kikunote.entity.Note
import com.kikunote.method.DateChange
import com.kikunote.viewModel.NotesViewModel
import java.util.*

class EditActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEditBinding
    val editNoteExtra = "edit_note_extra"
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var note: Note
    private var isUpdate = false
    private val dateChange = DateChange()
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendar = Calendar.getInstance()

        initView()
        initViewModel()
        initListener()

    }

    private fun initView() {
        if (intent.getParcelableExtra<Note>(editNoteExtra) != null) {
            isUpdate = true
            binding.buttonDelete.visibility = View.VISIBLE

            note = intent.getParcelableExtra(editNoteExtra)!!
            binding.editTextTitle.setText(note.title)
            binding.editTextBody.setText(note.body)
            binding.editTextTitle.setSelection(note.title.length)

            // Set spinner position
            val compareValue = note.label
            val adapter = ArrayAdapter.createFromResource(
                this,
                R.array.labels,
                android.R.layout.simple_spinner_item
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spLabel.adapter = adapter

            val spinnerPosition = adapter.getPosition(compareValue)
            binding.spLabel.setSelection(spinnerPosition)
        }
    }

    private fun initViewModel() {
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
    }

    private fun initListener() {
        binding.toolbar.nibBack.setOnClickListener(this)
        binding.toolbar.btnSave.setOnClickListener(this)
        binding.buttonDelete.setOnClickListener(this)
        binding.buttonSetReminder.setOnClickListener(this)  // Reminder Button
    }

    private fun showDialog() {
        AwesomeDialog.build(this)
            .position(AwesomeDialog.POSITIONS.CENTER)
            .body("The note will be permanently deleted.", color = ContextCompat.getColor(this, R.color.colorTitle))
            .background(R.drawable.background_dialog)
            .icon(R.mipmap.ic_launcher)
            .onPositive(
                "Yes, delete",
                buttonBackgroundColor = android.R.color.transparent,
                textColor = ContextCompat.getColor(this, R.color.colorTitle)
            ) {
                deleteNote(note)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
            .onNegative(
                "Cancel",
                buttonBackgroundColor = R.drawable.bg_btn_black,
                textColor = ContextCompat.getColor(this, R.color.background)
            ) {

            }
    }

    private fun deleteNote(note: Note) {
        notesViewModel.deleteNote(note)
        Toast.makeText(this@EditActivity, "Note removed", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.nib_back -> {
                onBackPressed()
            }
            R.id.btn_save -> {
                saveNote()
            }
            R.id.button_delete -> {
                showDialog()
            }
            R.id.button_set_reminder -> {
                showDateTimePicker()  // Handle Reminder Button Click
            }
        }
    }

    // Function to save note
    private fun saveNote() {
        val title = binding.editTextTitle.text.toString()
        val body = binding.editTextBody.text.toString()
        val label = binding.spLabel.selectedItem.toString()
        val date = dateChange.getToday()
        val time = dateChange.getTime()

        if (title.isEmpty() && body.isEmpty()) {
            Toast.makeText(this@EditActivity, "Note cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            if (isUpdate) {
                notesViewModel.updateNote(
                    Note(
                        id = note.id,
                        title = title,
                        label = label,
                        date = note.date,
                        time = note.time,
                        updatedDate = date,
                        updatedTime = time,
                        body = body
                    )
                )
            } else {
                notesViewModel.insertNote(
                    Note(
                        title = title,
                        label = label,
                        date = date,
                        time = time,
                        body = body
                    )
                )
            }

            Toast.makeText(this@EditActivity, "Note saved", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    // Show DateTime Picker for Reminder
    private fun showDateTimePicker() {
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                setReminder(calendar)  // After selecting date and time, set reminder
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePickerDialog.show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    // Function to set reminder
    private fun setReminder(calendar: Calendar) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use setExactAndAllowWhileIdle to ensure it triggers during Doze Mode
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(this, "Reminder set for: ${calendar.time}", Toast.LENGTH_SHORT).show()
    }
}
