# Parisara-Cycle – Green Commuter Guide

A full-stack Android application for the **MindMatrix VTU Internship Program** that helps students and workers safely use bicycles for short-distance commuting while promoting eco-friendly transportation and road safety awareness.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-green)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore%20%7C%20RTDB-orange)

## Features

| Module | Description |
|--------|-------------|
| **Splash & Auth** | Professional splash, Firebase email/password auth, registration, forgot password, session persistence |
| **Dashboard** | Welcome card, eco metrics, quick actions, achievements preview |
| **Safe Route Planner** | Google Maps, bicycling routes, polylines, safety score, danger/report markers |
| **Eco Stats** | 120g CO₂/km calculation, daily/weekly/monthly stats, achievement badges |
| **Pit-Stop Finder** | Repair shops, water stations, rest areas, filters, map markers |
| **Buddy System** | Real-time location sharing via Firebase Realtime Database |
| **Report Issues** | Potholes, blockages, etc. with image upload to Firebase Storage |
| **Admin Panel** | Verify/resolve reports, add danger zones (admin users only) |
| **Generative AI** | Gemini-powered personalized safety tips and eco messages |
| **Push Notifications** | FCM service for alerts, buddy requests, milestones |

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3
- **Architecture:** MVVM + Repository Pattern
- **Backend:** Firebase (Auth, Firestore, Storage, Realtime Database, FCM)
- **Maps:** Google Maps SDK, Directions API (bicycling mode)
- **Networking:** Retrofit, OkHttp
- **Async:** Kotlin Coroutines, Flow

## Project Structure

```
app/src/main/java/com/parisara/cycle/
├── data/
│   ├── model/          # Data classes
│   ├── remote/         # Retrofit APIs (Directions, Gemini)
│   └── repository/     # Firebase & API repositories
├── di/                 # AppContainer (DI)
├── service/            # FCM messaging
├── ui/
│   ├── components/     # Reusable Compose UI
│   ├── navigation/     # NavHost & routes
│   ├── screens/        # All app screens
│   ├── theme/          # Green & white theme
│   └── viewmodel/      # ViewModels
├── util/               # Mappers, polyline decoder, location utils
├── MainActivity.kt
└── ParisaraApp.kt
```

## Firestore Collections

| Collection | Purpose |
|------------|---------|
| `users` | Profile, stats, achievements, isAdmin |
| `rides` | Ride history with distance, CO₂, polyline |
| `reports` | Road issues (Pending/Verified/Resolved) |
| `pitStops` | Repair, water, rest, first-aid locations |
| `dangerZones` | Unsafe areas for map markers |
| `buddyRequests` | Buddy request workflow |
| `notifications` | In-app notification feed |

**Realtime Database:** `buddyLocations/{userId}` for live location sharing.

## Setup Instructions

### 1. Prerequisites

- Android Studio Ladybug (2024.2+) or newer
- JDK 17
- Android SDK 35
- Google account for Firebase & Cloud Console

### 2. Clone & Open

```bash
git clone <your-repo-url>
cd parisara
```

Open the project folder in **Android Studio** and let Gradle sync.

### 3. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create project **parisara-cycle**
3. Add Android app with package name: `com.parisara.cycle`
4. Download `google-services.json` → place in `app/google-services.json`
5. Enable **Authentication** → Email/Password
6. Create **Firestore Database** (production mode, then deploy rules from `firestore.rules`)
7. Create **Realtime Database** (deploy rules from `database.rules.json`)
8. Enable **Storage** for report images and profile photos (deploy `storage.rules`)
9. Enable **Cloud Messaging** for push notifications

Deploy all Firebase rules at once (requires [Firebase CLI](https://firebase.google.com/docs/cli)):

```bash
firebase deploy --only firestore:rules,firestore:indexes,database,storage
```

### 4. Google Maps & APIs

1. [Google Cloud Console](https://console.cloud.google.com/) → enable:
   - Maps SDK for Android
   - Directions API
   - (Optional) Places API
2. Create API key with Android app restriction (package + SHA-1)
3. Copy `local.properties.example` → `local.properties`:

```properties
sdk.dir=C\:\\Users\\YOUR_USER\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=your_google_maps_api_key
GEMINI_API_KEY=your_gemini_api_key
```

### 5. Admin User (Optional)

In Firestore, set on your user document:

```json
{ "isAdmin": true }
```

This unlocks the **Admin Panel** in the dashboard.

### 6. Run the App

1. Connect Android device or start emulator (API 26+)
2. Click **Run** ▶ in Android Studio
3. Register a new account or use test credentials you create in Firebase Auth

## Demo / Testing Without API Keys

The app includes **fallback behavior** when API keys are empty:

- Routes use straight-line distance estimation
- Pit-stops use built-in Bengaluru sample data
- AI tips use local default messages

For full demonstration, configure Maps and Gemini keys.

## CO₂ Calculation

```
CO₂ saved (grams) = distance (km) × 120
```

Example: 5 km ride → 600g CO₂ saved

## Achievements

| Badge | Requirement |
|-------|-------------|
| Green Beginner | 5 km total distance |
| Eco Warrior | ~8.33 km (1 kg CO₂ equivalent) |
| Cycle Champion | 100 km total |

## Screenshots Flow

```
Splash → Login/Register → Dashboard → [Route | Eco | Pit-Stops | Buddy | Report | Profile | ...]
```

## VTU Internship Presentation Tips

1. Demo registration and dashboard eco metrics
2. Plan a sample route on the map with safety score
3. Show pit-stop markers and buddy location sharing
4. Submit a road report with photo
5. Display AI-generated safety tip
6. Show admin panel (if isAdmin enabled)

## License

Educational project for MindMatrix VTU Internship Program.

## Author

Developed as a final-year engineering / internship demonstration project.
