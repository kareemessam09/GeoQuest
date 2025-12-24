# 🗺️ GeoQuest - GPS Treasure Hunt Game

A location-based Android game where players hunt for virtual treasures at real-world coordinates. Built with modern Android development practices demonstrating proficiency in GPS/Location services, Geofencing, and clean architecture.

## 🎮 Features

### Core Gameplay
- **Interactive Map** - OpenStreetMap integration with OSMDroid (no API key required)
- **Real-time Location Tracking** - FusedLocationProviderClient for accurate GPS
- **Geofencing API** - System-level proximity detection (100m radius)
- **"Hot & Cold" Navigation** - Visual and haptic feedback as you approach treasures
- **Treasure Collection** - Collect treasures when within 20 meters
- **Inventory System** - View collected treasures in backpack
- **Dynamic Treasure Spawning** - Treasures spawn randomly around user's location
- **Respawn Treasures** - Button to generate new treasure locations

### Technical Features
- **Foreground Service** - Background tracking with persistent notification
- **Proximity Notifications** - Get notified when near a treasure
- **Achievement System** - Unlockable achievements (First Find, Explorer, Speed Runner, etc.)
- **User Statistics** - Track distance walked, treasures collected, points earned
- **GPS Status Monitoring** - Detects GPS on/off changes with prompts
- **Dark/Light Theme** - Full theme support with system default option
- **Settings** - Haptic feedback, sound effects, notifications, distance units

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Kotlin** | Primary language |
| **Jetpack Compose** | Declarative UI |
| **Material 3** | Modern UI components |
| **Hilt** | Dependency Injection |
| **Room** | Local database |
| **DataStore** | Preferences storage |
| **Kotlin Flows/StateFlow** | Reactive state management |
| **OSMDroid** | OpenStreetMap SDK (free) |
| **FusedLocationProvider** | GPS location services |
?| **Geofencing API** | Proximity detection |
| **Foreground Service** | Background processing |
| **BroadcastReceiver** | System events (Boot, Geofence, GPS) |
| **Navigation Compose** | Screen navigation |

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │  MapScreen  │  │  Backpack   │  │  Achievements   │  │
│  └──────┬──────┘  └──────┬──────┘  └────────┬────────┘  │
│         │                │                   │           │
│  ┌──────┴──────┐  ┌──────┴──────┐  ┌────────┴────────┐  │
│  │GameViewModel│  │InventoryVM │  │ AchievementsVM  │  │
│  └──────┬──────┘  └──────┬──────┘  └────────┬────────┘  │
└─────────┼────────────────┼──────────────────┼───────────┘
          │                │                  │
┌─────────┼────────────────┼──────────────────┼───────────┐
│         │           Data Layer              │           │
│  ┌──────┴──────┐  ┌──────┴──────┐  ┌────────┴────────┐  │
│  │ LocationRepo│  │InventoryRepo│  │ AchievementRepo │  │
│  └──────┬──────┘  └──────┬──────┘  └────────┬────────┘  │
│         │                │                  │           │
│  ┌──────┴──────┐  ┌──────┴──────────────────┴────────┐  │
│  │FusedLocation│  │          Room Database           │  │
│  │  Provider   │  │  (Inventory, Achievements, Stats)│  │
│  └─────────────┘  └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

**Pattern:** MVVM (Model-View-ViewModel)  
**DI:** Hilt  
**State:** Kotlin StateFlow

## 📱 Screens

| Screen | Description |
|--------|-------------|
| **Map** | Main game view with treasures, location, and controls |
| **Backpack** | Inventory of collected treasures |
| **Achievements** | Progress and unlocked achievements |
| **Settings** | App preferences and theme |
| **Permissions** | Location permission flow |

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 28+ (Android 9.0)
- Physical device recommended for GPS testing

### Installation

```bash
git clone https://github.com/yourusername/GeoQuest.git
cd GeoQuest
```

Open in Android Studio → Sync Gradle → Run

> **Note:** No API keys required! Uses free OpenStreetMap.

### Required Permissions

| Permission | Purpose |
|------------|---------|
| `ACCESS_FINE_LOCATION` | Precise GPS |
| `ACCESS_COARSE_LOCATION` | Approximate location |
| `ACCESS_BACKGROUND_LOCATION` | Background tracking (Android 10+) |
| `POST_NOTIFICATIONS` | Proximity alerts (Android 13+) |
| `FOREGROUND_SERVICE_LOCATION` | Background service |
| `RECEIVE_BOOT_COMPLETED` | Re-register geofences after reboot |

## 📁 Project Structure

```
app/src/main/java/com/compose/geoquest/
├── GeoQuestApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/                  # Room database & DAOs
│   ├── model/                  # Data models
│   ├── preferences/            # DataStore preferences
│   └── repository/             # Repositories
├── di/                         # Hilt modules
├── receiver/                   # BroadcastReceivers
│   ├── BootReceiver.kt
│   ├── GeofenceBroadcastReceiver.kt
│   └── GpsStatusReceiver.kt
├── service/                    # Foreground service
│   └── GeofenceMonitorService.kt
├── util/                       # Utility classes
│   ├── GeofenceManager.kt
│   ├── HapticFeedbackManager.kt
│   ├── ProximityNotificationManager.kt
│   └── SoundManager.kt
└── ui/
    ├── game/                   # Map & game logic
    ├── inventory/              # Backpack
    ├── achievements/           # Achievements
    ├── settings/               # Settings
    ├── components/             # Reusable components
    ├── navigation/             # Navigation
    └── theme/                  # Theme & colors
```

## 🎯 Skills Demonstrated

- **Android Jetpack** - Compose, Room, DataStore, Navigation, Hilt
- **Location Services** - FusedLocationProvider, Geofencing API
- **Background Processing** - Foreground Service, BroadcastReceivers
- **Reactive Programming** - Kotlin Flows, StateFlow
- **Clean Architecture** - MVVM, Repository pattern, Dependency Injection
- **Modern Kotlin** - Coroutines, Sealed classes, Extension functions

## 📄 License

MIT License - see [LICENSE](LICENSE) file.

## 👨‍💻 Author

**Kareem**

---

<p align="center">
  Made with ❤️ in Egypt 🇪🇬
</p>
