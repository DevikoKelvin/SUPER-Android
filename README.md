# SUPER Android

**SUPER (Survey Product)** is an Android field data collection app by **PT Erlangga Edi Laboratories (Erela)**. It enables field surveyors to manage outlets, execute surveys, and track check-in/check-out attendance with GPS and photo documentation.

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| Build System | Gradle 9.0.0 (Kotlin DSL) |
| Min SDK | 26 (Android 8.0 Oreo) |
| Target / Compile SDK | 36 |
| Architecture | MVVM (ViewModel + LiveData) |
| Navigation | Jetpack Navigation Component |
| Networking | Retrofit 3 + OkHttp 5 + Gson |
| Image Loading | Glide 5 |
| Maps | Google Maps + MapLibre GL |
| Analytics / Crash | Firebase Analytics + Crashlytics |
| Animations | Rive, Shimmer |
| UI | Material Design 3, ViewBinding |

---

## Requirements

- Android Studio Meerkat or newer
- JDK 11
- Android device or emulator running Android 8.0 (API 26)+
- Google Maps API key
- MapTiler API key

---

## Getting Started

1. **Clone the repository** and open it in Android Studio.
2. **Sync Gradle** — Android Studio will download all dependencies automatically.
3. **Configure API keys** — API URLs and keys are injected via `BuildConfig` fields in [app/build.gradle.kts](app/build.gradle.kts). Update them as needed.
4. **Signing** — Release builds use `super_keystore.jks`. Place the keystore in the `app/` directory and verify the signing config in `build.gradle.kts`.
5. **Run** — Select a device/emulator and press Run.

### Release APK Naming

```
Erela_Super_release_v{versionName}.{versionCode}.apk
```

---

## Project Structure

```
app/src/main/
├── java/id/erela/surveyproduct/
│   ├── activities/         # 9 activities (Splash, Login, Main, CheckIn/Out, Survey, Outlet)
│   ├── fragments/          # 5 main fragments (Home, Outlet, Survey, History, Profile)
│   ├── adapters/           # RecyclerView + ViewPager2 adapters
│   ├── bottom_sheets/      # Filter history, select outlet
│   ├── dialogs/            # Loading, confirmation, photo preview
│   ├── helpers/
│   │   ├── api/            # Retrofit instances and endpoint interfaces
│   │   ├── customs/        # Custom Toast
│   │   └── ...             # Permission, SharedPreferences, UserData helpers
│   └── objects/            # 20+ data model classes (Gson @SerializedName)
├── res/
│   ├── layout/             # XML layouts
│   ├── navigation/         # Navigation graph
│   ├── values/             # Strings, colors, themes
│   ├── values-in/          # Indonesian translations
│   └── values-night/       # Dark mode styles
└── AndroidManifest.xml
```

---

## Screens & Features

### Activities

| Activity | Description |
|---|---|
| `SplashScreenActivity` | App entry point |
| `LoginActivity` | User authentication |
| `MainActivity` | Main hub with 5-tab bottom navigation |
| `CheckInActivity` | Record check-in with GPS + photo |
| `CheckOutActivity` | Record check-out with reward tracking |
| `SurveyDetailActivity` | Display survey form |
| `AnswerActivity` | Submit survey answers |
| `AddOutletActivity` | Create a new outlet |
| `DetailOutletActivity` | View and edit outlet info |

### Main Tabs (Fragments)

| Fragment | Description |
|---|---|
| Home | Dashboard overview |
| Outlet | Outlet listing and management |
| Start Survey | Available surveys |
| History | Check-in/out and survey history with date range filter |
| Profile | User profile and logout |

---

## API Integration

All requests include the custom header:

```
x-superapp-authorized: 2025::Er3L@n2e1pZVYgz8
```

### Erela Endpoint — `https://erela.id/api/super/`

| Method | Endpoint | Description |
|---|---|---|
| POST | `users/check` | Login authentication |

### Super Endpoint — `http://surveyor.erela.co.id:3000/super/api/`

**Users**
| Method | Endpoint | Description |
|---|---|---|
| GET | `user` | Get user by username |

**Surveys**
| Method | Endpoint | Description |
|---|---|---|
| GET | `survey` | List all active surveys |
| GET | `survey/history` | Answer history |
| GET | `survey/check` | Check existing answer |
| POST | `survey/answer` | Submit answer (multipart with photos) |

**Outlets**
| Method | Endpoint | Description |
|---|---|---|
| GET | `outlet` | List all outlets |
| GET | `outlet/{id}` | Outlet detail |
| GET | `outlet/category` | Outlet categories |
| GET | `outlet/province` | Province list |
| GET | `outlet/region` | Region list |
| POST | `outlet` | Create outlet |
| PUT | `outlet/{id}` | Update outlet |

**Check-in / Check-out**
| Method | Endpoint | Description |
|---|---|---|
| GET | `check` | All check-in/out records |
| GET | `check/today` | Today's record |
| GET | `check/is-checked` | Whether user is already checked in |
| GET | `check/is-15-minutes` | 15-minute cooldown validation |
| POST | `check/in` | Record check-in (multipart with photo) |
| POST | `check/out` | Record check-out (multipart, up to 3 photos) |

---

## Permissions

| Permission | Purpose |
|---|---|
| `INTERNET` | API communication |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | GPS for check-in/out |
| `CAMERA` | Photo capture for check-in/out and surveys |
| `READ_MEDIA_IMAGES` | Image picker (Android 13+) |
| `READ/WRITE_EXTERNAL_STORAGE` | Storage access (Android ≤ 12) |
| `POST_NOTIFICATIONS` | Push notifications |
| `REQUEST_INSTALL_PACKAGES` | In-app APK update |

---

## Build & Versioning

Version is managed via `buildNumber.properties`:

```
versionName=1.9
buildNumber=578
```

The full version code is assembled dynamically at build time. To release a new version, increment `buildNumber` and trigger a release build.

**ProGuard** is enabled for release builds with resource shrinking.

---

## Internationalization

- **English** — default (`res/values/`)
- **Indonesian** — `res/values-in/`
- **Dark Mode** — `res/values-night/`

---

## Firebase

- **Analytics** — usage tracking
- **Crashlytics** — crash reporting with NDK support

Configure via `google-services.json` placed in `app/`.

---

## License

Proprietary — Erela &copy; 2024
