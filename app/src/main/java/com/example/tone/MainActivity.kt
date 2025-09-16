package com.example.tone

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupMediaPlayer()
        checkPermissions()
    }

    private fun initializeViews() {
        seekBar = findViewById(R.id.seekBar)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        currentTime = findViewById(R.id.currentTime)
        totalTime = findViewById(R.id.totalTime)
        songTitle = findViewById(R.id.songTitle)
        artistName = findViewById(R.id.artistName)

        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnPrevious.setOnClickListener { playPrevious() }
        btnNext.setOnClickListener { playNext() }

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

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer()
        //sample music file
        val assetFileDescriptor = resources.openRawResourceFd(R.raw.bns)
        mediaPlayer.setDataSource(
            assetFileDescriptor.fileDescriptor,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.length
        )
        assetFileDescriptor.close()

        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            seekBar.max = mediaPlayer.duration
            totalTime.text = formatTime(mediaPlayer.duration)
            updateSongInfo("Sample Song", "Sample Artist")
        }

        mediaPlayer.setOnCompletionListener {
            btnPlayPause.setImageResource(R.drawable.ic_play)
            handler.removeCallbacks(updateSeekBar)
        }
    }

    private fun togglePlayPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            btnPlayPause.setImageResource(R.drawable.ic_play)
            handler.removeCallbacks(updateSeekBar)
        } else {
            mediaPlayer.start()
            btnPlayPause.setImageResource(R.drawable.ic_pause)
            handler.post(updateSeekBar)
        }
    }

    private fun playPrevious() {
        // Implement previous track logic
        mediaPlayer.seekTo(0)
        if (!mediaPlayer.isPlaying) {
            togglePlayPause()
        }
    }

    private fun playNext() {
        // Implement next track logic
        mediaPlayer.seekTo(0)
        if (!mediaPlayer.isPlaying) {
            togglePlayPause()
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBar)
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}