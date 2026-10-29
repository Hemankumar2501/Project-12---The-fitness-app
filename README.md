# Project 12 - 12-Week Transformation Tracker

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Android-SDK%2024%20--%2036-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-FF6F00?style=for-the-badge" alt="Architecture" />
  <img src="https://img.shields.io/badge/Database-Room%20(SQLite)-00599C?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room DB" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License" />
</p>

---

## Overview

**Project 12** is a comprehensive, offline-first native Android health and fitness application engineered with **Kotlin** and **Jetpack Compose**. It is specifically designed to guide users through an intensive **12-week body transformation** combining progressive overload tracking, everyday Indian nutrition planning, visual photo comparisons, habit monitoring, and intelligent coaching.

Built following modern Android development best practices, Project 12 features a reactive **MVVM architecture**, unidirectional data flow with **Coroutines and StateFlow**, local persistence via **Jetpack Room**, and sleek **Material 3** dark-mode aesthetics.

---

## Table of Contents

- [Key Features](#key-features)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Project Directory Structure](#project-directory-structure)
- [Data Model & Persistence](#data-model--persistence)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Build & Run](#build--run)
- [Privacy & Security](#privacy--security)
- [License](#license)

---

## Key Features

### 🏋️‍♂️ 1. Structured 12-Week Workout System
- **Periodized Splits**: Pre-seeded workout routines customized for 12 weeks:
  - *Monday*: Push Day (Chest, Shoulders & Triceps)
  - *Tuesday*: Legs & Core (Quads, Hamstrings, Glutes & Abs)
  - *Wednesday*: Active Recovery (Brisk Walking, Mobility & Joint Health)
  - *Thursday*: Pull Day (Back, Biceps & Rear Deltoids)
  - *Friday*: Full Body & Core Compound Strength
  - *Saturday*: Low-Impact Conditioning & Core
  - *Sunday*: Full Rest & Recovery
- **Live Session Tracking**: Log individual sets, reps, weight (kg), and RPE (perceived exertion).
- **Progressive Overload Analytics**: Automated calculations for total session volume, 1RM progression curves, and exercise history.
- **Exercise Substitution**: Built-in alternative recommendations when equipment is occupied or joint discomfort occurs.

### 🥗 2. Everyday Indian & Global Nutrition Tracker
- **Native Indian Food Database**: Pre-populated database featuring common regional dishes and staple foods:
  - Idli, Dosa, Sambar, Dal Tadka, Roti, Brown/White Rice
  - High-protein staples: Paneer, Curd, Soy Chunks, Boiled Eggs, Chicken Breast, Kala Chana, Sprouted Moong
- **Daily Macro Targets**: Dynamic calculation and real-time visualization of:
  - Calorie deficit/surplus goals
  - Protein intake targets (g)
  - Carbohydrates, Fats, and Dietary Fibre
- **Categorized Meal Logs**: Separate tracking for Breakfast, Mid-Morning Snack, Lunch, Evening Snack, and Dinner.

### 📸 3. Visual Transformation Journal & Biometrics
- **Multi-Angle Photo Logging**: Capture and review progress across three standard poses:
  - Front Pose
  - Side Profile
  - Back Pose
- **12-Week Timeline**: Filter progress photos by week (Weeks 1 to 12) for clear side-by-side visual comparisons.
- **Biometric History**: Log and chart changes in body weight (kg), waist, chest, hips, arms, and thighs.

### 🧠 4. Dedicated Transformation Coach
- **Intelligent Fitness Advisory**: Interactive in-app coaching specialist accessible directly from the bottom navigation.
- **Context-Aware Assistance**: Generates tailored advice using your current profile, active week, dietary preference, and workout logs.
- **Quick Action Prompts**: One-tap chips for high-protein meal suggestions, DOMS recovery tips, and exercise replacements.

### 💧 5. Daily Habit & Recovery Engine
- **Hydration Tracker**: Quick-add water log with target progress indicators.
- **Sleep & Recovery**: Track daily sleep duration and mobility habits.
- **Streak & Consistency Tracker**: Daily habit checkboxes for stretching and movement breaks.

---

## Architecture & Tech Stack

The app follows **Official Android Architecture Guidelines**, utilizing an **Offline-First MVVM (Model-View-ViewModel)** pattern with unidirectional data flow.

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│   Jetpack Compose Screens • Material 3 Components      │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────┴────────────────────────────┐
│                   ViewModel Layer                      │
│     AppViewModel • Coroutines • State Management       │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                  Repository Layer                      │
│               TransformationRepository                 │
└─────────────▲────────────────────────────▲─────────────┘
              │                            │
┌─────────────┴──────────────┐   ┌─────────┴─────────────┐
│     Local Data Source      │   │   Remote / Services   │
│   Room Database (SQLite)   │   │  Firestore Sync       │
│   14 DAOs & Seed Data      │   │  Coach Service        │
└────────────────────────────┘   └───────────────────────┘
```

### Core Technologies

| Category | Component / Library | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.2+ | Modern, expressive, and safe language |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative, modern Android UI toolkit |
| **Architecture** | MVVM + Repository Pattern | Clean separation of concerns and maintainability |
| **Local Storage** | Jetpack Room (v2.7) | Offline-first SQLite database with compile-time verification |
| **Asynchronous** | Kotlin Coroutines & Flow | Non-blocking background operations and reactive streams |
| **Image Loading** | Coil Compose (v2.7) | High-performance asynchronous image rendering |
| **Networking** | Retrofit 2 + Moshi + OkHttp | Type-safe REST client with JSON parsing |
| **Dependency Injection** | AndroidViewModel Architecture | Lifecycle-aware dependency scoping |
| **Cloud Integration** | Firebase (Auth, Firestore, App Check) | User identity, remote synchronization, and integrity checks |
| **Build Configuration** | Gradle Kotlin DSL + Version Catalog | Modern, type-safe dependency management via `libs.versions.toml` |

---

## Project Directory Structure

```text
project-12/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/          # Room Database, Type Converters, DAOs
│   │   │   │   │   │   ├── Daos.kt
│   │   │   │   │   │   └── Project12Database.kt
│   │   │   │   │   ├── model/          # Data Entities (Profile, Workout, Food, Photos)
│   │   │   │   │   │   └── Models.kt
│   │   │   │   │   ├── remote/         # Remote sync and Coaching services
│   │   │   │   │   │   ├── FirestoreSyncService.kt
│   │   │   │   │   │   └── GeminiCoachService.kt
│   │   │   │   │   └── repository/     # Unified TransformationRepository
│   │   │   │   │       └── TransformationRepository.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/           # Login, Signup, Guest Mode Screen
│   │   │   │   │   │   └── AuthScreen.kt
│   │   │   │   │   ├── navigation/     # AppNavigation & Bottom Navigation Bar
│   │   │   │   │   │   └── AppNavigation.kt
│   │   │   │   │   ├── onboarding/     # 12-Week Profile Setup Wizard
│   │   │   │   │   │   └── OnboardingWizard.kt
│   │   │   │   │   ├── screens/        # Core Application Screens
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── WorkoutsScreen.kt
│   │   │   │   │   │   ├── NutritionScreen.kt
│   │   │   │   │   │   ├── ProgressScreen.kt
│   │   │   │   │   │   ├── ProfileScreen.kt
│   │   │   │   │   │   └── CoachScreen.kt
│   │   │   │   │   ├── theme/          # Neon Lime & Midnight Dark Theme
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   └── viewmodel/      # AppViewModel & Navigation States
│   │   │   │   │       └── AppViewModel.kt
│   │   │   │   ├── util/               # App Check & Utility Extensions
│   │   │   │   └── MainActivity.kt     # Single Activity Entry Point
│   │   │   ├── res/                    # Drawables, mipmaps, XML configurations
│   │   │   └── AndroidManifest.xml
│   │   └── test/                       # Unit & Robolectric Tests
│   └── build.gradle.kts                # App-level build configurations
├── gradle/
│   └── libs.versions.toml              # Version Catalog (Dependencies & Plugins)
├── build.gradle.kts                    # Root build configuration
├── settings.gradle.kts                 # Project settings & repositories
└── README.md
```

---

## Data Model & Persistence

The application includes a relational SQLite database managed via **Room**, containing 14 normalized tables:

- `user_profile`: Physical stats, 12-week goal, dietary preferences, and target macros.
- `workout_templates` & `exercise_templates`: Phase-based workout splits and exercise instructions.
- `exercises`: Master library with muscle group categorization and default sets/reps.
- `workout_sessions` & `workout_logs`: Real-time session history, set-by-set weight/reps, and volume.
- `food_items`: Master catalog of regional and international foods with macro ratios.
- `meal_entries`: Daily food logs with timestamps, portions, and meal categorization.
- `cardio_entries`: Activity logs (steps, duration, distance, intensity, calories burned).
- `body_measurements`: Periodic circumference measurements (chest, waist, arms, thighs, hips).
- `photo_entries`: Local file references, week indexing, and pose types (`front`, `side`, `back`).
- `daily_habits`: Daily logs for hydration (ml), sleep duration, and mobility habits.
- `chat_messages`: Conversation history with the transformation coach.

---

## Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer recommended
- **Java Development Kit**: JDK 17 or JDK 21
- **Android SDK**: Compile SDK `36`, Minimum SDK `24` (Android 7.0+)
- **Device**: Android Emulator (Pixel 8 recommended) or physical Android device with USB Debugging enabled

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Hemankumar2501/Project-12---The-fitness-app.git
   cd Project-12---The-fitness-app
   ```

2. **Configure Environment Secrets**:
   Copy the example environment configuration to `.env`:
   ```bash
   cp .env.example .env
   ```
   Open `.env` and add your Gemini API key:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

3. **Open in Android Studio**:
   - Open Android Studio.
   - Click **Open** and select the `project-12` directory.
   - Wait for Gradle to synchronize dependencies.

---

## Build & Run

### From Android Studio
- Select the `app` run configuration in the top toolbar.
- Choose your target device (emulator or physical phone).
- Press **Run** (`Shift + F10`) or **Debug** (`Shift + F9`).

### From Command Line (Gradle)

- **Assemble Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```
  The generated APK will be available at:
  `app/build/outputs/apk/debug/app-debug.apk`

- **Install directly on connected device**:
  ```bash
  ./gradlew installDebug
  ```

- **Run Unit Tests**:
  ```bash
  ./gradlew testDebugUnitTest
  ```

---

## Privacy & Security

- **Local Storage by Default**: Progress photos, personal measurements, and workout logs are saved directly to the device's sandboxed internal storage.
- **Integrity Verification**: Protected via Firebase App Check to ensure only genuine, untampered app instances communicate with backend services.
- **Secure Key Management**: Secrets Gradle Plugin ensures API keys and sensitive tokens are injected during build time from `.env` and not committed into version control.

---

## License

This project is licensed under the [MIT License](LICENSE). Feel free to use, modify, and distribute this codebase for personal and commercial projects.
