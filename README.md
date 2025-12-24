# 🗺️ GeoQuest - GPS Treasure Hunt Game

<p align="center">
  <img src="screenshots/logo.png" width="200" alt="GeoQuest Logo"/>
</p>

A location-based augmented reality (AR) style Android game where players hunt for virtual treasures at real-world coordinates. Built with modern Android development practices to demonstrate proficiency in GPS/Location services, reactive programming, and clean architecture.

## 🎮 Features

### Core Gameplay
- **Interactive Map** - OpenStreetMap integration with OSMDroid (no API key required!)
- **Real-time Location Tracking** - FusedLocationProviderClient for accurate GPS
- **"Hot & Cold" Navigation** - Visual and haptic feedback as you approach treasures
- **Geofencing** - Treasures unlock only when within 15 meters
- **Inventory System** - Collect and view your treasures in a backpack

### Advanced Features
- **Achievement System** - Gamification with unlockable achievements
- **User Statistics** - Track distance walked, treasures collected, and more
- **Settings & Preferences** - DataStore-backed preferences (haptics, theme, units)
- **Debug Teleport Mode** - Tap-to-teleport for testing without walking
- **Analytics Abstraction** - Clean analytics layer ready for Firebase/Amplitude

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Kotlin** | Primary language |
| **Jetpack Compose** | Modern declarative UI |
| **Hilt** | Dependency Injection |
| **Room** | Local database for inventory & achievements |
| **DataStore** | Preferences storage |
| **Kotlin Flows** | Reactive state management |
| **OSMDroid** | Free OpenStreetMap SDK |
| **FusedLocationProvider** | GPS location services |
| **Navigation Compose** | Screen navigation |
| **Material 3** | Modern UI components |

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
**DI:** Hilt for dependency injection  
**State:** Kotlin StateFlow for reactive UI updates

## 📱 Screenshots

| Map Screen | Hot & Cold | Treasure Found |
|------------|------------|----------------|
| ![Map](screenshots/map.png) | ![Proximity](screenshots/hot_cold.png) | ![Found](screenshots/treasure_found.png) |

| Backpack | Achievements | Settings |
|----------|--------------|----------|
| ![Backpack](screenshots/backpack.png) | ![Achievements](screenshots/achievements.png) | ![Settings](screenshots/settings.png) |

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 28+ (Android 9.0)
- Physical device recommended (for GPS testing)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/GeoQuest.git
   cd GeoQuest
   ```

2. **Open in Android Studio**
   - File → Open → Select the project folder

3. **Sync Gradle**
   - Click "Sync Now" when prompted

4. **Run the app**
   - Select your device/emulator
   - Click Run ▶️

> **Note:** No API keys required! The app uses OpenStreetMap which is completely free.

### Customizing Treasure Locations

Edit `TreasureRepository.kt` to add treasures near your location:

```kotlin
Treasure(
    id = "my_treasure",
    name = "My Custom Treasure",
    latitude = YOUR_LATITUDE,
    longitude = YOUR_LONGITUDE,
    reward = TreasureReward(
        type = RewardType.GOLD,
        name = "Custom Reward",
        value = 100
    )
)
```

## 🧪 Testing

Run unit tests:
```bash
./gradlew test
```

Tests cover:
- ✅ Proximity level calculations
- ✅ Achievement system logic
- ✅ Analytics event tracking
- ✅ Model validation

## 📁 Project Structure

```
app/src/main/java/com/compose/geoquest/
├── GeoQuestApplication.kt      # Hilt Application
├── MainActivity.kt             # Entry point
├── analytics/                  # Analytics abstraction
│   └── AnalyticsTracker.kt
├── data/
│   ├── local/                  # Room database
│   │   ├── GeoQuestDatabase.kt
│   │   ├── InventoryDao.kt
│   │   └── AchievementDao.kt
│   ├── model/                  # Data models
│   │   ├── GameState.kt
│   │   ├── Treasure.kt
│   │   ├── Achievement.kt
│   │   └── InventoryItem.kt
│   ├── preferences/            # DataStore
│   │   └── UserPreferences.kt
│   └── repository/             # Data repositories
│       ├── LocationRepository.kt
│       ├── InventoryRepository.kt
│       ├── TreasureRepository.kt
│       └── AchievementRepository.kt
├── di/                         # Hilt modules
│   └── AppModule.kt
├── util/                       # Utilities
│   └── HapticFeedbackManager.kt
└── ui/
    ├── game/                   # Main game screen
    ├── inventory/              # Backpack screen
    ├── achievements/           # Achievements screen
    ├── settings/               # Settings screen
    ├── navigation/             # Navigation setup
    └── theme/                  # Material theme
```

## 🎯 CV Highlights

This project demonstrates:

- **Android Jetpack** - Compose, Room, DataStore, Navigation, Hilt
- **Location Services** - FusedLocationProvider, Geofencing concepts
- **Reactive Programming** - Kotlin Flows, StateFlow
- **Clean Architecture** - MVVM, Repository pattern, DI
- **Testing** - Unit tests with JUnit, MockK
- **Modern Kotlin** - Coroutines, Sealed classes, Extension functions
- **Production Patterns** - Analytics abstraction, Preference management

## 🗺️ Roadmap

- [ ] Multiplayer leaderboard (Firebase)
- [ ] AR treasure view (ARCore)
- [ ] Background geofence notifications
- [ ] Offline map caching
- [ ] Social sharing

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/yourprofile)

---

<p align="center">
  Made with ❤️ in Egypt 🇪🇬
</p>

