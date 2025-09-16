package com.example.tone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnSelectFolders: ImageButton
    private lateinit var currentTime: TextView
    private lateinit var totalTime: TextView
    private lateinit var songTitle: TextView
    private lateinit var artistName: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBar = object : Runnable {
        override fun run() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
                currentTime.text = formatTime(mediaPlayer.currentPosition)
            }
            handler.postDelayed(this, 1000)
        }
    }

    private var songsList = mutableListOf<Song>()
    private var currentSongIndex = 0
    private var selectedFolders: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        checkPermissions()
    }

    private fun initializeViews() {
        seekBar = findViewById(R.id.seekBar)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnSelectFolders = findViewById(R.id.btnSelectFolders)
        currentTime = findViewById(R.id.currentTime)
        totalTime = findViewById(R.id.totalTime)
        songTitle = findViewById(R.id.songTitle)
        artistName = findViewById(R.id.artistName)

        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnPrevious.setOnClickListener { playPrevious() }
        btnNext.setOnClickListener { playNext() }
        btnSelectFolders.setOnClickListener { openFolderSelection() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && ::mediaPlayer.isInitialized) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadSongs() {
        songsList.clear()

        if (selectedFolders.isNotEmpty()) {
            // Load songs from selected folders
            for (folder in selectedFolders) {
                val folderSongs = MusicLoader.getAllAudioFromDevice(this, folder)
                songsList.addAll(folderSongs)
            }
        } else {
            // Load all songs if no specific folders selected
            songsList = MusicLoader.getAllAudioFromDevice(this).toMutableList()
        }

        if (songsList.isNotEmpty()) {
            setupMediaPlayer(songsList[0])
            Toast.makeText(this, "Found ${songsList.size} songs", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No songs found. Click the folder icon to select music folders.", Toast.LENGTH_LONG).show()
            // Don't auto-open folder selection, just show message
        }
    }

    private fun openFolderSelection() {
        val intent = Intent(this, FolderSelectionActivity::class.java)
        startActivityForResult(intent, FOLDER_SELECTION_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FOLDER_SELECTION_REQUEST && resultCode == RESULT_OK) {
            selectedFolders = data?.getStringArrayListExtra("selectedFolders") ?: emptyList()
            loadSongs() // Reload songs with new folder selection
        }
    }

    private fun setupMediaPlayer(song: Song) {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.reset()
        } else {
            mediaPlayer = MediaPlayer()
        }

        try {
            mediaPlayer.setDataSource(song.path)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener {
                seekBar.max = mediaPlayer.duration
                totalTime.text = formatTime(mediaPlayer.duration)
                updateSongInfo(song.title, song.artist)
            }

            mediaPlayer.setOnCompletionListener {
                playNext()
            }

            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Toast.makeText(this, "Error playing: ${song.title}", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error playing: ${song.title}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePlayPause() {
        if (songsList.isEmpty()) {
            Toast.makeText(this, "No songs available. Select folders first.", Toast.LENGTH_SHORT).show()
            return
        }

        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            btnPlayPause.setImageResource(R.drawable.ic_play)
            handler.removeCallbacks(updateSeekBar)
        } else {
            if (!::mediaPlayer.isInitialized) {
                setupMediaPlayer(songsList[currentSongIndex])
            }
            mediaPlayer.start()
            btnPlayPause.setImageResource(R.drawable.ic_pause)
            handler.post(updateSeekBar)
        }
    }

    private fun playPrevious() {
        if (songsList.isEmpty()) return

        currentSongIndex--
        if (currentSongIndex < 0) {
            currentSongIndex = songsList.size - 1
        }

        setupMediaPlayer(songsList[currentSongIndex])
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.start()
            handler.post(updateSeekBar)
        }
    }

    private fun playNext() {
        if (songsList.isEmpty()) return

        currentSongIndex++
        if (currentSongIndex >= songsList.size) {
            currentSongIndex = 0
        }

        setupMediaPlayer(songsList[currentSongIndex])
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.start()
            handler.post(updateSeekBar)
        }
    }

    private fun updateSongInfo(title: String, artist: String) {
        songTitle.text = title
        artistName.text = artist
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    1
                )
            } else {
                loadSongs()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                loadSongs()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs()
            } else {
                Toast.makeText(this, "Permission denied. Cannot load music.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBar)
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }

    companion object {
        private const val FOLDER_SELECTION_REQUEST = 100
    }
}