# Oralvis-Client-Clinic

Native Android app for OralVis Healthcare Clinic — built with **Jetpack Compose**, **MVVM**, and **Clean Architecture** (data → domain → ui).

## Stack

- Kotlin, Jetpack Compose
- Retrofit + OkHttp (cookie-based auth, token refresh on 401)
- Coroutines, StateFlow, ViewModel
- Single NavHost, floating bottom navigation

## Build

Requires **JDK 17**.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)  # macOS
./gradlew assembleDebug
```

## Project structure

- `app/` — Android application (core, data, domain, ui)
- `docs/` — Backend API and feature analysis
- `oralvis_back/` — Backend reference (do not modify)

## License

Proprietary — OralVis Healthcare.
