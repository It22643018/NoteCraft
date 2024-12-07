package com.kikunote.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.NoteCraft.helper.SharedPreferencesHelper
import com.example.awesomedialog.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kikunote.R
import com.kikunote.databinding.ActivityDetailNoteBinding
import com.kikunote.databinding.BottomsheetNoteBinding
import com.kikunote.method.DateChange
import com.kikunote.model.Note

class DetailNoteActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityDetailNoteBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var noteId: String
    private val dateChange = DateChange()
    private lateinit var dialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesHelper = SharedPreferencesHelper(this)  // Initialize SharedPreferencesHelper

        initView()
        initListener()

    }

    private fun initView() {
        // Get note ID from Intent and load the note from SharedPreferences
        noteId = intent.getStringExtra("note_id") ?: ""

        val note = sharedPreferencesHelper.getNote(noteId)

        if (note != null) {
            binding.tvTitle.text = note.title
            binding.tvDate.text = dateChange.changeFormatDate(note.date)
            binding.tvBody.text = note.body

            if (note.updatedDate.isNotEmpty()) {
                binding.tvEdited.text =
                    "Last edit ${dateChange.changeFormatDate(note.updatedDate)}"
            } else {
                binding.tvEdited.text =
                    "Last edit ${dateChange.changeFormatDate(note.date)}"
            }
        }
    }

    private fun initListener() {
        binding.toolbar.nibBack.setOnClickListener(this)
        binding.toolbar.nibEdit.setOnClickListener(this)
        binding.ibMenu.setOnClickListener(this)
    }

    private fun deleteNote() {
        sharedPreferencesHelper.deleteNote(noteId)
        Toast.makeText(this@DetailNoteActivity, "Note removed", Toast.LENGTH_SHORT).show()
    }

    private fun showBottomSheet() {
        val views: View =
            LayoutInflater.from(this).inflate(R.layout.bottomsheet_note, null)

        val bindingBottom = BottomsheetNoteBinding.bind(views)

        dialog = BottomSheetDialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(views)
        dialog.show()

        bindingBottom.clDelete.setOnClickListener(this)
        bindingBottom.clShare.setOnClickListener(this)
    }

    private fun showDialog() {
        AwesomeDialog.build(this)
            .position(AwesomeDialog.POSITIONS.CENTER)
            .body(
                "The note will be permanently deleted.",
                color = ContextCompat.getColor(this, R.color.colorTitle)
            )
            .background(R.drawable.background_dialog)
            .icon(R.mipmap.ic_launcher)
            .onPositive(
                "Yes, delete",
                buttonBackgroundColor = android.R.color.transparent,
                textColor = ContextCompat.getColor(this, R.color.colorTitle)
            ) {
                deleteNote()
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
                // Do nothing
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.nib_back -> {
                onBackPressed()
            }
            R.id.nib_edit -> {
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("note_id", noteId)
                startActivity(intent)
            }
            R.id.ib_menu -> {
                showBottomSheet()
            }
            R.id.cl_delete -> {
                showDialog()
            }
            R.id.cl_share -> {
                val note = sharedPreferencesHelper.getNote(noteId)
                if (note != null) {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.body}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
            }
        }
    }
}
