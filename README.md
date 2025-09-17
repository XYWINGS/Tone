# 🎶 Tone - Android Music Player

A beautiful and functional music player for Android built with Kotlin and Android Studio.  
Features a modern UI with album art background blur effects, folder-based music library management, and smooth playback controls.

---

## 🎵 Features
- **Music Library Scanning**: Automatically scans and indexes music files from device storage  
- **Folder Selection**: Choose specific folders to include in your music library  
- **Modern UI**: Clean interface with album art display and blurred background effects  
- **Playback Controls**: Play, pause, next, previous with seekbar functionality  
- **Background Playback**: Music continues playing when app is in background  
- **MediaStore Integration**: Efficient music file indexing using Android's MediaStore API  
- **Blur Effects**: Beautiful blurred album art backgrounds using Glide transformations  

---

## 🛠️ Technical Stack
- **Language**: Kotlin  
- **Minimum SDK**: Android 8.0 (API 26)  
- **Architecture**: MVC (Model-View-Controller)  
- **Image Loading**: Glide with transformations  
- **Media Playback**: Android MediaPlayer  
- **Storage Access**: MediaStore API with runtime permissions  

---

## 📋 Prerequisites
- Android Studio (latest version)  
- Android device or emulator with API 26+  
- Music files stored on device (MP3, M4A, WAV, FLAC)  

---

## 🔧 Installation

### Clone the repository
git clone https://github.com/XYWINGS/Tone.git 

## Open in Android Studio
Open Android Studio

Select "Open an existing project"

Navigate to the cloned directory

Build and Run
Connect your Android device or start an emulator

Click Run in Android Studio or press Ctrl + R

📱 Usage
First Launch
Grant storage permissions when prompted

The app will automatically scan for music files

Use the folder icon to select specific music folders

Tap play to start listening

Controls
Play/Pause: Center button to toggle playback

Next/Previous: Skip forward or backward through tracks

Seekbar: Drag to navigate through current song

Folder Selection: Folder icon to manage music library locations

Folder Management
Click the folder icon in the player controls

Select which folders to include in your music library

Confirm selection to reload your music library

🎨 UI Components
Album Art Display: High-quality album artwork with circular design

Blurred Background: Dynamic background based on current track's album art

Song Information: Title and artist name display

Progress Indicators: Current time and total duration

Control Cluster: Intuitive playback controls

🔐 Permissions
The app requires the following permissions:

READ_EXTERNAL_STORAGE (Android 12 and below)

READ_MEDIA_AUDIO (Android 13 and above)

FOREGROUND_SERVICE for background playback

WAKE_LOCK to keep device awake during playback

🏗️ Project Structure
bash
Copy code
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/tone/
│   │   │   ├── MainActivity.kt            # Main player interface
│   │   │   ├── FolderSelectionActivity.kt # Folder management
│   │   │   ├── MusicLoader.kt             # Music file scanning utility
│   │   │   └── Song.kt                    # Data class for music tracks
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── activity_main.xml              # Player layout
│   │       │   └── activity_folder_selection.xml  # Folder selection UI
│   │       └── drawable/                          # Icons and graphics
└── build.gradle                            # Dependencies and configuration
📊 Key Components
MusicLoader.kt
Handles music file scanning using Android's MediaStore API. Features:

Folder-based music scanning

Metadata extraction (title, artist, album, duration)

Album art retrieval

Efficient cursor management

MainActivity.kt
Main player interface with:

MediaPlayer lifecycle management

Playback state handling

UI updates and synchronization

Background blur effects implementation

Song.kt
Data class representing music tracks.

🚀 Future Enhancements
Playlist creation and management

Equalizer and audio effects

Sleep timer functionality

Widget support for home screen controls

Theme customization (light/dark mode)

Lyrics display integration

Cloud storage integration

Crossfade between tracks
