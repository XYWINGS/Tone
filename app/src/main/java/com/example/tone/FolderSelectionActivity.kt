package com.example.tone

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FolderSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_selection)

        val foldersListView = findViewById<ListView>(R.id.foldersListView)
        val btnSelectAll = findViewById<Button>(R.id.btnSelectAll)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        // Get available music folders
        val folders = MusicLoader.getAvailableMusicFolders(this)

        if (folders.isEmpty()) {
            Toast.makeText(this, "No music folders found on device", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, folders)
        foldersListView.adapter = adapter
        foldersListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        // Select all by default
        for (i in 0 until foldersListView.count) {
            foldersListView.setItemChecked(i, true)
        }

        btnSelectAll.setOnClickListener {
            for (i in 0 until foldersListView.count) {
                foldersListView.setItemChecked(i, true)
            }
        }

        btnConfirm.setOnClickListener {
            val selectedFolders = mutableListOf<String>()
            for (i in 0 until foldersListView.count) {
                if (foldersListView.isItemChecked(i)) {
                    selectedFolders.add(folders[i])
                }
            }

            if (selectedFolders.isEmpty()) {
                Toast.makeText(this, "Please select at least one folder", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultIntent = Intent()
            resultIntent.putStringArrayListExtra("selectedFolders", ArrayList(selectedFolders))
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}